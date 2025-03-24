package repository;

import domain.Airline;
import domain.Plane;
import domain.Seat;
import domain.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaneRepository implements CrudRepository<Plane> {
    private final Connection postgresConn;
    private final Connection myConn;

    public PlaneRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTables();
    }

    private void createTables() {
        String createPlanesPostgreSQL = """
            CREATE TABLE IF NOT EXISTS planes (
            planeId SERIAL PRIMARY KEY,
            planeCode VARCHAR(10) UNIQUE NOT NULL,
            airlineId INTEGER NOT NULL,
            numOfSeats INTEGER NOT NULL,
            version INTEGER DEFAULT 1,
            FOREIGN KEY(airlineId) REFERENCES airlines(airlineId) ON DELETE CASCADE);""";
        String createPlanesMySQL = """
            CREATE TABLE IF NOT EXISTS planes (
            planeId INTEGER PRIMARY KEY AUTO_INCREMENT,
            planeCode VARCHAR(10) UNIQUE NOT NULL,
            airlineId INTEGER NOT NULL,
            numOfSeats INTEGER NOT NULL,
            version INTEGER DEFAULT 1,
            FOREIGN KEY(airlineId) REFERENCES airlines(airlineId) ON DELETE CASCADE);""";
        String createSeatsPostgreSQL = """
            CREATE TABLE IF NOT EXISTS seats (
            seatId SERIAL PRIMARY KEY,
            seatNr VARCHAR(5) NOT NULL,
            planeId INTEGER NOT NULL,
            seatType VARCHAR(20) NOT NULL,
            FOREIGN KEY(planeId) REFERENCES planes(planeId) ON DELETE CASCADE);""";
        String createSeatsMySQL = """
            CREATE TABLE IF NOT EXISTS seats (
            seatId INTEGER PRIMARY KEY AUTO_INCREMENT,
            seatNr VARCHAR(5) NOT NULL,
            planeId INTEGER NOT NULL,
            seatType VARCHAR(20) NOT NULL,
            FOREIGN KEY(planeId) REFERENCES planes(planeId) ON DELETE CASCADE);""";

        try {
            Statement createPostgresStatement = postgresConn.createStatement();
            Statement createMyStatement = myConn.createStatement();

            createPostgresStatement.executeUpdate(createPlanesPostgreSQL);
            createPostgresStatement.executeUpdate(createSeatsPostgreSQL);
            postgresConn.commit();

            createMyStatement.executeUpdate(createPlanesMySQL);
            createMyStatement.executeUpdate(createSeatsMySQL);
            myConn.commit();

        } catch (SQLException e) {
            try {
                postgresConn.rollback();
                myConn.rollback();
                System.out.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackException) {
                System.out.println("Rollback failed: " + rollbackException.getMessage());
            }
        }
    }

    @Override
    public void save(Plane entity) {
        String postgresQuery = "INSERT INTO planes (planeCode, airlineId, numOfSeats, version) VALUES (?, ?, ?, ?) RETURNING planeId";
        String myQuery = "INSERT INTO planes (planeCode, airlineId, numOfSeats, version) VALUES (?, ?, ?, ?)";
        String seatsQuery = "INSERT INTO seats (seatNr, planeId, seatType) VALUES (?, ?, ?)";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(postgresQuery);
            PreparedStatement seatsPostgresStmt = postgresConn.prepareStatement(seatsQuery);
            PreparedStatement myStmt = myConn.prepareStatement(myQuery);
            PreparedStatement seatsMyStmt = myConn.prepareStatement(seatsQuery);

            postgresStmt.setString(1, entity.getPlaneCode());
            postgresStmt.setInt(2, entity.getAirline().getAirlineId());
            postgresStmt.setInt(3, entity.getNumOfSeats());
            postgresStmt.setInt(4, 1);
            ResultSet prs = postgresStmt.executeQuery();
            postgresConn.commit();

            myStmt.setString(1, entity.getPlaneCode());
            myStmt.setInt(2, entity.getAirline().getAirlineId());
            myStmt.setInt(3, entity.getNumOfSeats());
            myStmt.setInt(4, 1);
            myStmt.executeUpdate();
            myConn.commit();

            if (prs.next()) {
                int planeId = prs.getInt("planeId");

                for(Seat s : entity.getSeatList()) {
                    seatsPostgresStmt.setString(1, s.getSeatNr());
                    seatsPostgresStmt.setInt(2, planeId);
                    seatsPostgresStmt.setString(3, s.getSeatType().toString());
                    seatsPostgresStmt.executeUpdate();
                    postgresConn.commit();

                    seatsMyStmt.setString(1, s.getSeatNr());
                    seatsMyStmt.setInt(2, planeId);
                    seatsMyStmt.setString(3, s.getSeatType().toString());
                    seatsMyStmt.executeUpdate();
                    myConn.commit();
                }
            }

        } catch (SQLException e) {
            try {
                postgresConn.rollback();
                myConn.rollback();
                System.out.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackException) {
                System.out.println("Rollback failed: " + rollbackException.getMessage());
            }
        }
    }

    @Override
    public Plane findById(int id) {
        String query = """
            SELECT p.*, a.*
            FROM planes p
            JOIN airlines a ON a.airlineId = p.airlineId
            WHERE planeId = ?""";
        String query2 = "SELECT * FROM seats WHERE planeId = ?";
        Plane plane = null;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            List<Seat> seats = new ArrayList<>();

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Airline airline = new Airline(rs.getInt("airlineId"), rs.getString("airlineName"), rs.getString("email"), rs.getString("phone"));
                plane = new Plane(
                        rs.getInt("planeId"),
                        rs.getString("planeCode"),
                        airline,
                        rs.getInt("numOfSeats")
                );

                stmt2.setInt(1, id);
                ResultSet rs2 = stmt2.executeQuery();

                while (rs2.next()) {
                    seats.add(new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    ));
                }

                plane.setSeatList(seats);
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return plane;
    }

    @Override
    public List<Plane> findAll() {
        List<Plane> planes = new ArrayList<>();
        String query = """
            SELECT p.*, a.*
            FROM planes p
            JOIN airlines a ON a.airlineId = p.airlineId
            """;
        String query2 = "SELECT * FROM seats WHERE planeId = ?";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Airline airline = new Airline(rs.getInt("airlineId"), rs.getString("airlineName"), rs.getString("email"), rs.getString("phone"));
                Plane plane = new Plane(
                        rs.getInt("planeId"),
                        rs.getString("planeCode"),
                        airline,
                        rs.getInt("numOfSeats")
                );

                stmt2.setInt(1, plane.getPlaneId());
                ResultSet rs2 = stmt2.executeQuery();
                List<Seat> seats = new ArrayList<>();

                while (rs2.next()) {
                    seats.add(new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    ));
                }

                plane.setSeatList(seats);
                planes.add(plane);
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return planes;
    }

    @Override
    public void update(Plane entity) {
        String select = "SELECT * FROM planes WHERE planeId = ? FOR UPDATE";
        String query = """
            UPDATE planes
            SET planeCode = ?, version = version + 1
            WHERE planeId = ? AND version = ?
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getPlaneId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setString(1, entity.getPlaneCode());
                postgresStmt.setInt(2, entity.getPlaneId());
                postgresStmt.setInt(3, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getPlaneId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setString(1, entity.getPlaneCode());
                myStmt.setInt(2, entity.getPlaneId());
                myStmt.setInt(3, mrs.getInt("version"));
                myStmt.executeUpdate();
            }

            postgresConn.commit();
            myConn.commit();

        } catch (SQLException e) {
            try {
                postgresConn.rollback();
                myConn.rollback();
                System.out.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackException) {
                System.out.println("Rollback failed: " + rollbackException.getMessage());
            }
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM planes WHERE planeId = ?";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement myStmt = myConn.prepareStatement(query);

            postgresStmt.setInt(1, id);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            myStmt.setInt(1, id);
            myStmt.executeUpdate();
            myConn.commit();

        } catch (SQLException e) {
            try {
                postgresConn.rollback();
                myConn.rollback();
                System.out.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackException) {
                System.out.println("Rollback failed: " + rollbackException.getMessage());
            }
        }
    }
}

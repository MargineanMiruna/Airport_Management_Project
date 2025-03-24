package repository;

import domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookingRepository implements CrudRepository<Booking> {
    private final Connection postgresConn;
    private final Connection myConn;

    public BookingRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTable();
    }

    private void createTable() {
        String createPostgreSQL = """
            CREATE TABLE IF NOT EXISTS bookings (
            bookingId SERIAL PRIMARY KEY,
            flightId INTEGER NOT NULL,
            passengerId INTEGER NOT NULL,
            bookingTime TIMESTAMP NOT NULL,
            seatId INTEGER NOT NULL,
            bags VARCHAR(100),
            version INTEGER DEFAULT 1,
            FOREIGN KEY(flightId) REFERENCES flights(flightId) ON DELETE CASCADE,
            FOREIGN KEY(passengerId) REFERENCES passengers(passengerId) ON DELETE CASCADE,
            FOREIGN KEY(seatId) REFERENCES seats(seatId) ON DELETE CASCADE);""";
        String createMySQL = """
            CREATE TABLE IF NOT EXISTS bookings (
            bookingId INTEGER PRIMARY KEY AUTO_INCREMENT,
            flightId INTEGER NOT NULL,
            passengerId INTEGER NOT NULL,
            bookingTime TIMESTAMP NOT NULL,
            seatId INTEGER NOT NULL,
            bags VARCHAR(100),
            version INTEGER DEFAULT 1,
            FOREIGN KEY(flightId) REFERENCES flights(flightId) ON DELETE CASCADE,
            FOREIGN KEY(passengerId) REFERENCES passengers(passengerId) ON DELETE CASCADE,
            FOREIGN KEY(seatId) REFERENCES seats(seatId) ON DELETE CASCADE);""";

        try {
            Statement createPostgresStatement = postgresConn.createStatement();
            Statement createMyStatement = myConn.createStatement();

            createPostgresStatement.executeUpdate(createPostgreSQL);
            postgresConn.commit();

            createMyStatement.executeUpdate(createMySQL);
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
    public void save(Booking entity) {
        String query = "INSERT INTO bookings (flightId, passengerId, bookingTime, seatId, bags, version) VALUES (?, ?, ?, ?, ?, ?)";
        String bags = entity.getBags().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement myStmt = myConn.prepareStatement(query);

            postgresStmt.setInt(1, entity.getFlight().getFlightId());
            postgresStmt.setInt(2, entity.getPassenger().getPassengerId());
            postgresStmt.setTimestamp(3, Timestamp.valueOf(entity.getBookingTime()));
            postgresStmt.setInt(4, entity.getSeat().getSeatId());
            postgresStmt.setString(5, bags);
            postgresStmt.setInt(6, 1);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            myStmt.setInt(1, entity.getFlight().getFlightId());
            myStmt.setInt(2, entity.getPassenger().getPassengerId());
            myStmt.setTimestamp(3, Timestamp.valueOf(entity.getBookingTime()));
            myStmt.setInt(4, entity.getSeat().getSeatId());
            myStmt.setString(5, bags);
            myStmt.setInt(6, 1);
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

    @Override
    public Booking findById(int id) {
        String query = """
            SELECT b.*, f.*, p.*,
                   o.airportId AS originId, o.airportName AS originName, o.airportCode AS originCode, o.city AS originCity, o.country AS originCountry,
                   d.airportId AS destinationId, d.airportName AS destinationName, d.airportCode AS destinationCode, d.City AS destinationCity, d.country AS destinationCountry
            FROM bookings b
            JOIN flights f ON b.flightId = f.flightId
            JOIN passengers p ON b.passengerId = p.passengerId
            JOIN airports o ON f.origin = o.airportId
            JOIN airports d ON f.destination = d.airportId
            WHERE bookingId = ?""";
        String query2 = """
            SELECT s.*, p.*
            FROM seats s
            JOIN planes p ON p.planeId = s.planeId
            JOIN airline a ON a.airlineId = p.airlineId
            WHERE seatId = ?""";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stmt2.setInt(1, rs.getInt("seatId"));
                ResultSet rs2 = stmt2.executeQuery();

                if (rs2.next()) {
                    Airline airline = new Airline(rs2.getInt("airlineId"), rs2.getString("airlineName"), rs2.getString("email"), rs2.getString("phone"));
                    Plane plane = new Plane(rs2.getInt("planeId"), rs2.getString("planeCode"), airline, rs2.getInt("numOfSeats"));
                    Seat seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    );

                    String bagsString = rs.getString("bags");
                    List<Bag> bags = Arrays.stream(bagsString.split(","))
                            .map(Bag::valueOf)
                            .collect(Collectors.toList());

                    Airport origin = new Airport(rs.getInt("originId"), rs.getString("originName"), rs.getString("originCode"), rs.getString("originCity"), rs.getString("originCountry"));
                    Airport destination = new Airport(rs.getInt("destinationId"), rs.getString("destinationName"), rs.getString("destinationCode"), rs.getString("destinationCity"), rs.getString("destinationCountry"));
                    Flight flight = new Flight(rs.getInt("flightId"), airline, plane, rs.getTimestamp("departure").toLocalDateTime(), rs.getTimestamp("arrival").toLocalDateTime(), origin, destination, rs.getDouble("price"));
                    Passenger passenger = new Passenger(rs.getInt("passengerId"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getDate("birthDate").toLocalDate(), rs.getString("city"), rs.getString("country"));
                    return new Booking(
                            rs.getInt("bookingId"),
                            flight,
                            passenger,
                            rs.getTimestamp("bookingTime").toLocalDateTime(),
                            seat,
                            bags
                    );
                }
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookings = new ArrayList<>();
        String query = """
            SELECT b.*, f.*, p.*,
                   o.airportId AS originId, o.airportName AS originName, o.airportCode AS originCode, o.city AS originCity, o.country AS originCountry,
                   d.airportId AS destinationId, d.airportName AS destinationName, d.airportCode AS destinationCode, d.City AS destinationCity, d.country AS destinationCountry
            FROM bookings b
            JOIN flights f ON b.flightId = f.flightId
            JOIN passengers p ON b.passengerId = p.passengerId
            JOIN airports o ON f.origin = o.airportId
            JOIN airports d ON f.destination = d.airportId""";
        String query2 = """
            SELECT s.*, p.*
            FROM seats s
            JOIN planes p ON p.planeId = s.planeId
            JOIN airline a ON a.airlineId = p.airlineId
            WHERE seatId = ?""";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);

            while (rs.next()) {
                stmt2.setInt(1, rs.getInt("seatId"));
                ResultSet rs2 = stmt2.executeQuery();

                if (rs2.next()) {
                    Airline airline = new Airline(rs2.getInt("airlineId"), rs2.getString("airlineName"), rs2.getString("email"), rs2.getString("phone"));
                    Plane plane = new Plane(rs2.getInt("planeId"), rs2.getString("planeCode"), airline, rs2.getInt("numOfSeats"));
                    Seat seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    );

                    String bagsString = rs.getString("bags");
                    List<Bag> bags = Arrays.stream(bagsString.split(","))
                            .map(Bag::valueOf)
                            .collect(Collectors.toList());

                    Airport origin = new Airport(rs.getInt("originId"), rs.getString("originName"), rs.getString("originCode"), rs.getString("originCity"), rs.getString("originCountry"));
                    Airport destination = new Airport(rs.getInt("destinationId"), rs.getString("destinationName"), rs.getString("destinationCode"), rs.getString("destinationCity"), rs.getString("destinationCountry"));
                    Flight flight = new Flight(rs.getInt("flightId"), airline, plane, rs.getTimestamp("departure").toLocalDateTime(), rs.getTimestamp("arrival").toLocalDateTime(), origin, destination, rs.getDouble("price"));
                    Passenger passenger = new Passenger(rs.getInt("passengerId"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getDate("birthDate").toLocalDate(), rs.getString("city"), rs.getString("country"));
                    bookings.add(new Booking(
                            rs.getInt("bookingId"),
                            flight,
                            passenger,
                            rs.getTimestamp("bookingTime").toLocalDateTime(),
                            seat,
                            bags
                    ));
                }
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return bookings;
    }

    @Override
    public void update(Booking entity) {
        String select = "SELECT * FROM bookings WHERE bookingId = ? FOR UPDATE";
        String query = """
            UPDATE bookings
            SET seatId = ?, bags = ?, version = version + 1
            WHERE bookingId = ? AND version = ?
        """;
        String bags = entity.getBags().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getBookingId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setInt(1, entity.getSeat().getSeatId());
                postgresStmt.setString(2, bags);
                postgresStmt.setInt(3, entity.getBookingId());
                postgresStmt.setInt(4, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getBookingId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setInt(1, entity.getSeat().getSeatId());
                myStmt.setString(2, bags);
                myStmt.setInt(3, entity.getBookingId());
                myStmt.setInt(4, mrs.getInt("version"));
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
        String query = "DELETE FROM bookings WHERE bookingId = ?";

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

package repository;

import domain.Bag;
import domain.Booking;
import domain.Seat;
import domain.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookingRepository implements CrudRepository<Booking> {
    private final Connection postgresConn;
    private final Connection myConn;
    private final FlightRepository flightRepository;
    private final PlaneRepository planeRepository;
    private final PassengerRepository passengerRepository;

    public BookingRepository(Connection postgresConn, Connection myConn, FlightRepository flightRepository, PlaneRepository planeRepository, PassengerRepository passengerRepository) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTable();

        this.flightRepository = flightRepository;
        this.planeRepository = planeRepository;
        this.passengerRepository = passengerRepository;
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
        String query = "INSERT INTO bookings (flightId, passengerId, bookingTime, seatId, bags, version) VALUES (%s, %s, %s, %s, %s, %s)";
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
        String query = "SELECT * FROM bookings WHERE bookingId = %s";
        String query2 = "SELECT * FROM seats WHERE seatId = %s";

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
                    Seat seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            planeRepository.findById(rs2.getInt("planeId")),
                            SeatType.valueOf(rs2.getString("seatType"))
                    );

                    String bagsString = rs.getString("bags");
                    List<Bag> bags = Arrays.stream(bagsString.split(","))
                            .map(Bag::valueOf)
                            .collect(Collectors.toList());

                    return new Booking(
                            rs.getInt("bookingId"),
                            flightRepository.findById(rs.getInt("flightId")),
                            passengerRepository.findById(rs.getInt("passengerId")),
                            rs.getTimestamp("bookingTime").toLocalDateTime(),
                            seat,
                            bags
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM bookings";
        String query2 = "SELECT * FROM seats WHERE seatId = %s";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);

            while (rs.next()) {
                stmt2.setInt(1, rs.getInt("seatId"));
                ResultSet rs2 = stmt2.executeQuery();

                if (rs2.next()) {
                    Seat seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            planeRepository.findById(rs2.getInt("planeId")),
                            SeatType.valueOf(rs2.getString("seatType"))
                    );

                    String bagsString = rs.getString("bags");
                    List<Bag> bags = Arrays.stream(bagsString.split(","))
                            .map(Bag::valueOf)
                            .collect(Collectors.toList());

                    bookings.add(new Booking(
                            rs.getInt("bookingId"),
                            flightRepository.findById(rs.getInt("flightId")),
                            passengerRepository.findById(rs.getInt("passengerId")),
                            rs.getTimestamp("bookingTime").toLocalDateTime(),
                            seat,
                            bags
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return bookings;
    }

    @Override
    public void update(Booking entity) {
        String select = "SELECT * FROM bookings WHERE bookingId = %s FOR UPDATE";
        String query = """
            UPDATE bookings
            SET seatId = %s, bags = %s, version = version + 1
            WHERE bookingId = %s AND version = %s
        """;
        String bags = entity.getBags().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getBookingId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            postgresStmt.setInt(1, entity.getSeat().getSeatId());
            postgresStmt.setString(2, bags);
            postgresStmt.setInt(3, entity.getBookingId());
            postgresStmt.setInt(4, prs.getInt("version"));
            postgresStmt.executeUpdate();
            postgresConn.commit();

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getBookingId());
            ResultSet mrs = mySelectStmt.executeQuery();

            PreparedStatement myStmt = myConn.prepareStatement(query);
            myStmt.setInt(1, entity.getSeat().getSeatId());
            myStmt.setString(2, bags);
            myStmt.setInt(3, entity.getBookingId());
            myStmt.setInt(4, mrs.getInt("version"));
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
    public void delete(int id) {
        String query = "DELETE FROM bookings WHERE bookingId = %s";

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

package repository;

import database.DatabaseConnection;
import domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookingRepository implements CrudRepository<Booking> {
    private Connection conn;

    public BookingRepository() {
        conn = DatabaseConnection.getMySQLConnection();
        createTable();
    }

    private void createTable() {
        String createSQL = "CREATE TABLE IF NOT EXISTS bookings (bookingId INT PRIMARY KEY AUTO_INCREMENT, flightId INT NOT NULL, passengerId INT NOT NULL, bookingTime TIMESTAMP NOT NULL, seatId INT NOT NULL, bags VARCHAR(100));";
        try (Statement createStatement = conn.createStatement()) {
            createStatement.executeUpdate(createSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Booking entity) {
        String query = "INSERT INTO bookings (flightId, passengerId, bookingTime, seatId, bags) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, entity.getFlight().getFlightId());
            stmt.setInt(2, entity.getPassenger().getPassengerId());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getBookingTime()));
            stmt.setInt(4, entity.getSeat().getSeatId());

            String bags = entity.getBags().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));

            stmt.setString(5, bags);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Booking findById(int id) {
        String query = "SELECT * FROM bookings WHERE bookingId = ?";
        String query2 = "SELECT * FROM seats WHERE seatId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2)) {

            FlightRepository flightRepository = new FlightRepository();
            PassengerRepository passengerRepository = new PassengerRepository();
            PlaneRepository planeRepository = new PlaneRepository();
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stmt2.setInt(1, rs.getInt("seatId"));
                ResultSet rs2 = stmt2.executeQuery();
                Seat seat = null;
                if (rs2.next()) {
                    seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            planeRepository.findById(rs2.getInt("planeId")),
                            SeatType.valueOf(rs2.getString("seatType"))
                    );
                }

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Booking> findAll() {
        String query = "SELECT * FROM bookings";
        String query2 = "SELECT * FROM seats WHERE seatId = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            FlightRepository flightRepository = new FlightRepository();
            PassengerRepository passengerRepository = new PassengerRepository();
            PlaneRepository planeRepository = new PlaneRepository();

            while (rs.next()) {
                stmt2.setInt(1, rs.getInt("seatId"));
                ResultSet rs2 = stmt2.executeQuery();
                Seat seat = null;
                if (rs2.next()) {
                    seat = new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            planeRepository.findById(rs2.getInt("planeId")),
                            SeatType.valueOf(rs2.getString("seatType"))
                    );
                }

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return bookings;
    }

    @Override
    public void update(Booking entity) {
        String query = "UPDATE bookings SET seatId = ?, bags = ? WHERE bookingId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, entity.getSeat().getSeatId());

            String bags = entity.getBags().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));

            stmt.setString(2, bags);
            stmt.setInt(3, entity.getBookingId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM bookings WHERE bookingId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

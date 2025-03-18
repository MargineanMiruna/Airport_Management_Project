package repository;

import database.DatabaseConnection;
import domain.Flight;
import domain.Seat;
import domain.SeatType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightRepository implements CrudRepository<Flight> {
    public FlightRepository() {
        createTables();
    }

    private void createTables() {
        String createFlightsSQL = "CREATE TABLE IF NOT EXISTS flights (flightId SERIAL PRIMARY KEY, airlineId INT NOT NULL, planeId INT NOT NULL, departure TIME NOT NULL, arrival TIME NOT NULL, origin INT NOT NULL, destination INT NOT NULL, price DOUBLE NOT NULL);";
        String createAvailableSeatsSQL = "CREATE TABLE IF NOT EXISTS available_seats (seatId INT NOT NULL, flightId INT NOT NULL, PRIMARY KEY (seatId, flightId));";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement createStatement = conn.createStatement()) {
            createStatement.executeUpdate(createFlightsSQL);
            createStatement.executeUpdate(createAvailableSeatsSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Flight entity) {
        String query = "INSERT INTO flights (airlineId, planeId, departure, arrival, origin, destination, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query2 = "INSERT INTO available_seats (seatId, flightId) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            stmt.setInt(1, entity.getAirline().getAirlineId());
            stmt.setInt(2, entity.getPlane().getPlaneId());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getDeparture()));
            stmt.setTimestamp(4, Timestamp.valueOf(entity.getArrival()));
            stmt.setInt(5, entity.getOrigin().getAirportId());
            stmt.setInt(6, entity.getDestination().getAirportId());
            stmt.setDouble(7, entity.getPrice());
            stmt.executeUpdate();

            for(Seat s : entity.getAvailableSeats()) {
                stmt2.setInt(1, s.getSeatId());
                stmt2.setInt(2, entity.getFlightId());
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Flight findById(int id) {
        String query = "SELECT * FROM flights WHERE flightId = ?";
        String query2 = "SELECT * FROM seats S" +
                "JOIN available_seats A ON A.seatId = S.seatId " +
                "WHERE A.flightId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            AirlineRepository airlineRepository = new AirlineRepository();
            PlaneRepository planeRepository = new PlaneRepository();
            AirportRepository airportRepository = new AirportRepository();
            Flight flight = null;
            List<Seat> availableSeats = new ArrayList<>();

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            ResultSet rs2 = stmt2.executeQuery();

            if (rs.next()) {
                flight = new Flight(
                        rs.getInt("flightId"),
                        airlineRepository.findById(rs.getInt("airlineId")),
                        planeRepository.findById(rs.getInt("planeId")),
                        rs.getTimestamp("departure").toLocalDateTime(),
                        rs.getTimestamp("arrival").toLocalDateTime(),
                        airportRepository.findById(rs.getInt("origin")),
                        airportRepository.findById(rs.getInt("destination")),
                        rs.getDouble("price")
                );
            }

            while (rs2.next()) {
                availableSeats.add(new Seat(
                        rs2.getInt("seatId"),
                        rs2.getString("seatNr"),
                        planeRepository.findById(rs2.getInt("planeId")),
                        SeatType.valueOf(rs2.getString("seatType"))
                ));
            }

            flight.setAvailableSeats(availableSeats);
            return flight;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Flight> findAll() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT * FROM flights";
        String query2 = "SELECT * FROM seats S" +
                "JOIN available_seats A ON A.seatId = S.seatId" +
                " WHERE A.flightId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement stmt2 = conn.prepareStatement(query2);
             ResultSet rs = stmt.executeQuery(query)) {

            AirlineRepository airlineRepository = new AirlineRepository();
            PlaneRepository planeRepository = new PlaneRepository();
            AirportRepository airportRepository = new AirportRepository();

            while (rs.next()) {
                Flight flight = new Flight(
                        rs.getInt("flightId"),
                        airlineRepository.findById(rs.getInt("airlineId")),
                        planeRepository.findById(rs.getInt("planeId")),
                        rs.getTimestamp("departure").toLocalDateTime(),
                        rs.getTimestamp("arrival").toLocalDateTime(),
                        airportRepository.findById(rs.getInt("origin")),
                        airportRepository.findById(rs.getInt("destination")),
                        rs.getDouble("price")
                );

                stmt2.setInt(1, flight.getFlightId());
                ResultSet rs2 = stmt2.executeQuery();
                List<Seat> availableSeats = new ArrayList<>();

                availableSeats.add(new Seat(
                        rs2.getInt("seatId"),
                        rs2.getString("seatNr"),
                        planeRepository.findById(rs2.getInt("planeId")),
                        SeatType.valueOf(rs2.getString("seatType"))
                ));

                flight.setAvailableSeats(availableSeats);
                flights.add(flight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

    @Override
    public void update(Flight entity) {
        String query = "UPDATE flights SET departure = ?, arrival = ?, price = ? WHERE flightId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(entity.getDeparture()));
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getArrival()));
            stmt.setDouble(3, entity.getPrice());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAvailableSeats(Flight entity) {
        String query = "SELECT * FROM seats S" +
                "JOIN available_seats A ON A.seatId = S.seatId" +
                "WHERE A.flightId = ?";
        String query2 = "DELETE FROM available_seats WHERE seatId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2)) {

            PlaneRepository planeRepository = new PlaneRepository();
            stmt.setInt(1, entity.getFlightId());
            ResultSet rs = stmt.getResultSet();

            while(rs.next()) {
                Seat seat = new Seat(
                        rs.getInt("seatId"),
                        rs.getString("seatNr"),
                        planeRepository.findById(rs.getInt("planeId")),
                        SeatType.valueOf(rs.getString("seatType"))
                );

                if(!entity.getAvailableSeats().contains(seat)) {
                    stmt2.setInt(1, seat.getSeatId());
                    stmt2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM flights WHERE flightId = ?";
        String query2 = "DELETE FROM available_seats WHERE flightId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt2.setInt(1, id);
            stmt2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

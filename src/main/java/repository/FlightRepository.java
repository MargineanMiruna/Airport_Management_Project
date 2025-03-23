package repository;

import domain.Flight;
import domain.Seat;
import domain.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightRepository implements CrudRepository<Flight> {
    private final Connection postgresConn;
    private final Connection myConn;
    private final AirlineRepository airlineRepository;
    private final PlaneRepository planeRepository;
    private final AirportRepository airportRepository;

    public FlightRepository(Connection postgresConn, Connection myConn, AirlineRepository airlineRepository, PlaneRepository planeRepository, AirportRepository airportRepository) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTables();

        this.airlineRepository = airlineRepository;
        this.planeRepository = planeRepository;
        this.airportRepository = airportRepository;
    }

    private void createTables() {
        String createFlightsPostgreSQL = """
            CREATE TABLE IF NOT EXISTS flights (
            flightId SERIAL PRIMARY KEY,
            airlineId INTEGER NOT NULL,
            planeId INTEGER NOT NULL,
            departure TIME NOT NULL,
            arrival TIME NOT NULL,
            origin INTEGER NOT NULL,
            destination INTEGER NOT NULL,
            price DOUBLE PRECISION NOT NULL,
            version INTEGER DEFAULT 1,
            FOREIGN KEY(airlineId) REFERENCES airlines(airlineId) ON DELETE CASCADE,
            FOREIGN KEY(planeId) REFERENCES planes(planeId) ON DELETE CASCADE,
            FOREIGN KEY(origin) REFERENCES airports(airportId) ON DELETE CASCADE,
            FOREIGN KEY(destination) REFERENCES airports(airportId) ON DELETE CASCADE);""";
        String createFlightsMySQL = """
            CREATE TABLE IF NOT EXISTS flights (
            flightId INTEGER PRIMARY KEY AUTO_INCREMENT,
            airlineId INTEGER NOT NULL,
            planeId INTEGER NOT NULL,
            departure TIME NOT NULL,
            arrival TIME NOT NULL,
            origin INTEGER NOT NULL,
            destination INTEGER NOT NULL,
            price DOUBLE PRECISION NOT NULL,
            version INTEGER DEFAULT 1,
            FOREIGN KEY(airlineId) REFERENCES airlines(airlineId) ON DELETE CASCADE,
            FOREIGN KEY(planeId) REFERENCES planes(planeId) ON DELETE CASCADE,
            FOREIGN KEY(origin) REFERENCES airports(airportId) ON DELETE CASCADE,
            FOREIGN KEY(destination) REFERENCES airports(airportId) ON DELETE CASCADE);""";
        String createAvailableSeatsSQL = """
            CREATE TABLE IF NOT EXISTS available_seats (
            seatId INTEGER NOT NULL,
            flightId INTEGER NOT NULL,
            PRIMARY KEY (seatId, flightId),
            FOREIGN KEY(seatId) REFERENCES seats(seatId) ON DELETE CASCADE,
            FOREIGN KEY(flightId) REFERENCES flights(flightId) ON DELETE CASCADE);""";

        try {
            Statement createPostgresStatement = postgresConn.createStatement();
            Statement createMyStatement = myConn.createStatement();

            createPostgresStatement.executeUpdate(createFlightsPostgreSQL);
            createPostgresStatement.executeUpdate(createAvailableSeatsSQL);
            postgresConn.commit();

            createMyStatement.executeUpdate(createFlightsMySQL);
            createMyStatement.executeUpdate(createAvailableSeatsSQL);
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
    public void save(Flight entity) {
        String query = "INSERT INTO flights (airlineId, planeId, departure, arrival, origin, destination, price, version) VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING flightId";
        String query2 = "INSERT INTO available_seats (seatId, flightId) VALUES (%s, %s)";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement postgresStmt2 = postgresConn.prepareStatement(query2);
            PreparedStatement myStmt = myConn.prepareStatement(query);
            PreparedStatement myStmt2 = myConn.prepareStatement(query2);

            postgresStmt.setInt(1, entity.getAirline().getAirlineId());
            postgresStmt.setInt(2, entity.getPlane().getPlaneId());
            postgresStmt.setTimestamp(3, Timestamp.valueOf(entity.getDeparture()));
            postgresStmt.setTimestamp(4, Timestamp.valueOf(entity.getArrival()));
            postgresStmt.setInt(5, entity.getOrigin().getAirportId());
            postgresStmt.setInt(6, entity.getDestination().getAirportId());
            postgresStmt.setDouble(7, entity.getPrice());
            postgresStmt.setInt(8, 1);
            ResultSet rs = postgresStmt.executeQuery();
            postgresConn.commit();

            myStmt.setInt(1, entity.getAirline().getAirlineId());
            myStmt.setInt(2, entity.getPlane().getPlaneId());
            myStmt.setTimestamp(3, Timestamp.valueOf(entity.getDeparture()));
            myStmt.setTimestamp(4, Timestamp.valueOf(entity.getArrival()));
            myStmt.setInt(5, entity.getOrigin().getAirportId());
            myStmt.setInt(6, entity.getDestination().getAirportId());
            myStmt.setDouble(7, entity.getPrice());
            myStmt.setInt(8, 1);
            myStmt.executeUpdate();
            myConn.commit();

            if (rs.next()) {
                int flightId = rs.getInt("flightId");

                for(Seat s : entity.getAvailableSeats()) {
                    postgresStmt2.setInt(1, s.getSeatId());
                    postgresStmt2.setInt(2, flightId);
                    postgresStmt2.executeUpdate();
                    postgresConn.commit();

                    myStmt2.setInt(1, s.getSeatId());
                    myStmt2.setInt(2, flightId);
                    myStmt2.executeUpdate();
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
    public Flight findById(int id) {
        String query = "SELECT * FROM flights WHERE flightId = %s";
        String query2 = """
            SELECT * FROM seats S
            JOIN available_seats A ON A.seatId = S.seatId
            WHERE A.flightId = %s
        """;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
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

                List<Seat> availableSeats = new ArrayList<>();
                stmt2.setInt(1, id);
                ResultSet rs2 = stmt2.executeQuery();

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
            }
        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Flight> findAll() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT * FROM flights";
        String query2 = """
            SELECT * FROM seats S
            JOIN available_seats A ON A.seatId = S.seatId
            WHERE A.flightId = %s
        """;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            Statement stmt = postgresConn.createStatement();
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            ResultSet rs = stmt.executeQuery(query);

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

                while (rs2.next()) {
                    availableSeats.add(new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            planeRepository.findById(rs2.getInt("planeId")),
                            SeatType.valueOf(rs2.getString("seatType"))
                    ));
                }

                flight.setAvailableSeats(availableSeats);
                flights.add(flight);
            }
        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return flights;
    }

    @Override
    public void update(Flight entity) {
        String select = "SELECT * FROM flights WHERE flightId = %s FOR UPDATE";
        String query = """
            UPDATE flights
            SET departure = %s, arrival = %s, price = %s, version = version + 1
            WHERE flightId = %s AND version = %s
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getFlightId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            postgresStmt.setTimestamp(1, Timestamp.valueOf(entity.getDeparture()));
            postgresStmt.setTimestamp(2, Timestamp.valueOf(entity.getArrival()));
            postgresStmt.setDouble(3, entity.getPrice());
            postgresStmt.setInt(4, entity.getFlightId());
            postgresStmt.setInt(5, prs.getInt("version"));
            postgresStmt.executeUpdate();
            postgresConn.commit();

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getFlightId());
            ResultSet mrs = mySelectStmt.executeQuery();

            PreparedStatement myStmt = myConn.prepareStatement(query);
            myStmt.setTimestamp(1, Timestamp.valueOf(entity.getDeparture()));
            myStmt.setTimestamp(2, Timestamp.valueOf(entity.getArrival()));
            myStmt.setDouble(3, entity.getPrice());
            myStmt.setInt(4, entity.getFlightId());
            myStmt.setInt(5, mrs.getInt("version"));
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

    public void updateAvailableSeats(Flight entity) {
        String select = "SELECT * FROM available_seats WHERE flightId = %s FOR UPDATE";
        String delete = "DELETE FROM available_seats WHERE flightId = %s";
        String insert = "INSERT INTO available_seats (seatId, flightId) VALUES (%s, %s)";

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            PreparedStatement postgresDeleteStmt = postgresConn.prepareStatement(delete);
            PreparedStatement myDeleteStmt = myConn.prepareStatement(delete);
            PreparedStatement postgresInsertStmt = postgresConn.prepareStatement(insert);
            PreparedStatement myInsertStmt = myConn.prepareStatement(insert);

            postgresSelectStmt.setInt(1, entity.getFlightId());
            ResultSet prs = postgresSelectStmt.executeQuery();
            mySelectStmt.setInt(1, entity.getFlightId());
            ResultSet mrs = mySelectStmt.executeQuery();

            postgresDeleteStmt.setInt(1, entity.getFlightId());
            postgresDeleteStmt.executeUpdate();
            myDeleteStmt.setInt(1, entity.getFlightId());
            myDeleteStmt.executeUpdate();

            for(Seat s : entity.getAvailableSeats()) {
                postgresInsertStmt.setInt(1, s.getSeatId());
                postgresInsertStmt.setInt(2, entity.getFlightId());
                postgresInsertStmt.executeUpdate();

                myInsertStmt.setInt(1, s.getSeatId());
                myInsertStmt.setInt(2, entity.getFlightId());
                myInsertStmt.executeUpdate();
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
        String query = "DELETE FROM flights WHERE flightId = %s";

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

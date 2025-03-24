package repository;

import domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightRepository implements CrudRepository<Flight> {
    private final Connection postgresConn;
    private final Connection myConn;

    public FlightRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTables();
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
        String query = "INSERT INTO flights (airlineId, planeId, departure, arrival, origin, destination, price, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING flightId";
        String query2 = "INSERT INTO available_seats (seatId, flightId) VALUES (?, ?)";

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
        String query = """
            SELECT f.flightId, f.departure, f.arrival, f.price, a.airlineId, a.airlineName, a.email, a.phone, p.planeId, p.planeCode, p.numOfSeats,
                   o.airportId AS originId, o.airportName AS originName, o.airportCode AS originCode, o.city AS originCity, o.country AS originCountry,
                   d.airportId AS destinationId, d.airportName AS destinationName, d.airportCode AS destinationCode, d.City AS destinationCity, d.country AS destinationCountry
            FROM flights f
            JOIN airlines a ON f.airlineId = a.airlineId
            JOIN planes p ON f.planeId = p.planeId
            JOIN airports o ON f.origin = o.airportId
            JOIN airports d ON f.destination = d.airportId
            WHERE f.flightId = ?
        """;;
        String query2 = """
            SELECT * FROM seats S
            JOIN available_seats A ON A.seatId = S.seatId
            WHERE A.flightId = ?
        """;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Airline airline = new Airline(rs.getInt("airlineId"), rs.getString("airlineName"), rs.getString("email"), rs.getString("phone"));
                Plane plane = new Plane(rs.getInt("planeId"), rs.getString("planeCode"), airline, rs.getInt("numOfSeats"));
                Airport origin = new Airport(rs.getInt("originId"), rs.getString("originName"), rs.getString("originCode"), rs.getString("originCity"), rs.getString("originCountry"));
                Airport destination = new Airport(rs.getInt("destinationId"), rs.getString("destinationName"), rs.getString("destinationCode"), rs.getString("destinationCity"), rs.getString("destinationCountry"));
                Flight flight = new Flight(
                        rs.getInt("flightId"),
                        airline,
                        plane,
                        rs.getTimestamp("departure").toLocalDateTime(),
                        rs.getTimestamp("arrival").toLocalDateTime(),
                        origin,
                        destination,
                        rs.getDouble("price")
                );

                List<Seat> availableSeats = new ArrayList<>();
                stmt2.setInt(1, id);
                ResultSet rs2 = stmt2.executeQuery();

                while (rs2.next()) {
                    availableSeats.add(new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    ));
                }

                flight.setAvailableSeats(availableSeats);
                return flight;
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Flight> findAll() {
        List<Flight> flights = new ArrayList<>();
        String query = """
            SELECT f.flightId, f.departure, f.arrival, f.price, a.airlineId, a.airlineName, a.email, a.phone, p.planeId, p.planeCode, p.numOfSeats,
                   o.airportId AS originId, o.airportName AS originName, o.airportCode AS originCode, o.city AS originCity, o.country AS originCountry,
                   d.airportId AS destinationId, d.airportName AS destinationName, d.airportCode AS destinationCode, d.City AS destinationCity, d.country AS destinationCountry
            FROM flights f
            JOIN airlines a ON f.airlineId = a.airlineId
            JOIN planes p ON f.planeId = p.planeId
            JOIN airports o ON f.origin = o.airportId
            JOIN airports d ON f.destination = d.airportId
        """;
        String query2 = """
            SELECT s.* FROM seats s
            JOIN available_seats a ON a.seatId = s.seatId
            WHERE a.flightId = ?
        """;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            Statement stmt = postgresConn.createStatement();
            PreparedStatement stmt2 = postgresConn.prepareStatement(query2);
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Airline airline = new Airline(rs.getInt("airlineId"), rs.getString("airlineName"), rs.getString("email"), rs.getString("phone"));
                Plane plane = new Plane(rs.getInt("planeId"), rs.getString("planeCode"), airline, rs.getInt("numOfSeats"));
                Airport origin = new Airport(rs.getInt("originId"), rs.getString("originName"), rs.getString("originCode"), rs.getString("originCity"), rs.getString("originCountry"));
                Airport destination = new Airport(rs.getInt("destinationId"), rs.getString("destinationName"), rs.getString("destinationCode"), rs.getString("destinationCity"), rs.getString("destinationCountry"));
                Flight flight = new Flight(
                        rs.getInt("flightId"),
                        airline,
                        plane,
                        rs.getTimestamp("departure").toLocalDateTime(),
                        rs.getTimestamp("arrival").toLocalDateTime(),
                        origin,
                        destination,
                        rs.getDouble("price")
                );

                stmt2.setInt(1, flight.getFlightId());
                ResultSet rs2 = stmt2.executeQuery();
                List<Seat> availableSeats = new ArrayList<>();

                while (rs2.next()) {
                    availableSeats.add(new Seat(
                            rs2.getInt("seatId"),
                            rs2.getString("seatNr"),
                            plane,
                            SeatType.valueOf(rs2.getString("seatType"))
                    ));
                }

                flight.setAvailableSeats(availableSeats);
                flights.add(flight);
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return flights;
    }

    @Override
    public void update(Flight entity) {
        String select = "SELECT * FROM flights WHERE flightId = ? FOR UPDATE";
        String query = """
            UPDATE flights
            SET departure = ?, arrival = ?, price = ?, version = version + 1
            WHERE flightId = ? AND version = ?
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getFlightId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setTimestamp(1, Timestamp.valueOf(entity.getDeparture()));
                postgresStmt.setTimestamp(2, Timestamp.valueOf(entity.getArrival()));
                postgresStmt.setDouble(3, entity.getPrice());
                postgresStmt.setInt(4, entity.getFlightId());
                postgresStmt.setInt(5, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getFlightId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setTimestamp(1, Timestamp.valueOf(entity.getDeparture()));
                myStmt.setTimestamp(2, Timestamp.valueOf(entity.getArrival()));
                myStmt.setDouble(3, entity.getPrice());
                myStmt.setInt(4, entity.getFlightId());
                myStmt.setInt(5, mrs.getInt("version"));
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

    public void updateAvailableSeats(Flight entity) {
        String select = "SELECT * FROM available_seats WHERE flightId = ? FOR UPDATE";
        String delete = "DELETE FROM available_seats WHERE flightId = ?";
        String insert = "INSERT INTO available_seats (seatId, flightId) VALUES (?, ?)";

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
        String query = "DELETE FROM flights WHERE flightId = ?";

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

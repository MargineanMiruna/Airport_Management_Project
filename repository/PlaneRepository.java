package repository;

import database.DatabaseConnection;
import domain.Plane;
import domain.Seat;
import domain.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaneRepository implements CrudRepository<Plane> {
    public PlaneRepository() {
        createTables();
    }

    private void createTables() {
        String createPlanesSQL = "CREATE TABLE IF NOT EXISTS planes (planeId SERIAL PRIMARY KEY, planeCode VARCHAR(10) UNIQUE NOT NULL, airlineId INT NOT NULL, numOfSeats INT NOT NULL);";
        String createSeatsSQL = "CREATE TABLE IF NOT EXISTS seats (seatId SERIAL PRIMARY KEY, seatNr VARCHAR(5) NOT NULL, planeId INT NOT NULL, seatType VARCHAR(20) NOT NULL);";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement createStatement = conn.createStatement()) {
             createStatement.executeUpdate(createPlanesSQL);
             createStatement.executeUpdate(createSeatsSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Plane entity) {
        String query = "INSERT INTO planes (planeCode, airlineId, numOfSeats) VALUES (?, ?, ?)";
        String query2 = "INSERT INTO seats (seatNr, planeId, seatType) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            stmt.setString(1, entity.getPlaneCode());
            stmt.setInt(2, entity.getAirline().getAirlineId());
            stmt.setInt(3, entity.getNumOfSeats());
            stmt.executeUpdate();

            for(Seat s : entity.getSeatList()) {
                stmt2.setString(1, s.getSeatNr());
                stmt2.setInt(2, s.getPlane().getPlaneId());
                stmt2.setString(3, s.getSeatType().toString());
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Plane findById(int id) {
        String query = "SELECT * FROM planes WHERE planeId = ?";
        String query2 = "SELECT * FROM seats WHERE planeId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement stmt2 = conn.prepareStatement(query2);) {

            AirlineRepository airlineRepository = new AirlineRepository();
            Plane plane = null;
            List<Seat> seats = new ArrayList<>();

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            ResultSet rs2 = stmt2.executeQuery();

            if (rs.next()) {
                plane = new Plane(
                        rs.getInt("planeId"),
                        rs.getString("planeCode"),
                        airlineRepository.findById(rs.getInt("airlineId")),
                        rs.getInt("numOfSeats")
                );
            }

            while (rs2.next()) {
                seats.add(new Seat(
                        rs2.getInt("seatId"),
                        rs2.getString("seatNr"),
                        plane,
                        SeatType.valueOf(rs2.getString("seatType"))
                ));
            }

            plane.setSeatList(seats);
            return plane;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Plane> findAll() {
        List<Plane> planes = new ArrayList<>();
        String query = "SELECT * FROM planes";
        String query2 = "SELECT * FROM seats WHERE planeId = ?";

        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement stmt2 = conn.prepareStatement(query2);
             ResultSet rs = stmt.executeQuery(query)) {

            AirlineRepository airlineRepository = new AirlineRepository();

            while (rs.next()) {
                Plane plane = new Plane(
                        rs.getInt("planeId"),
                        rs.getString("planeCode"),
                        airlineRepository.findById(rs.getInt("airlineId")),
                        rs.getInt("numOfSeats")
                );

                stmt2.setInt(1, plane.getPlaneId());
                ResultSet rs2 = stmt2.executeQuery();
                List<Seat> seats = new ArrayList<>();

                seats.add(new Seat(
                        rs.getInt("seatId"),
                        rs.getString("seatNr"),
                        plane,
                        SeatType.valueOf(rs2.getString("seatType"))
                ));

                plane.setSeatList(seats);
                planes.add(plane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return planes;
    }

    @Override
    public void update(Plane entity) {
        String query = "UPDATE planes SET planeCode = ?, airlineId = ? WHERE planeId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, entity.getPlaneCode());
            stmt.setInt(2, entity.getAirline().getAirlineId());
            stmt.setInt(3, entity.getPlaneId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM planes WHERE planeId = ?";
        String query2 = "DELETE FROM seats WHERE planeId = ?";
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

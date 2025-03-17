package repository;

import database.DatabaseConnection;
import domain.Airport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirportRepository implements CrudRepository<Airport> {
    public AirportRepository() {
        createTable();
    }

    private void createTable() {
        String createSQL = "CREATE TABLE IF NOT EXISTS airports (airportId SERIAL PRIMARY KEY, airportName VARCHAR(100) NOT NULL, airportCode VARCHAR(5) UNIQUE NOT NULL, city VARCHAR(100) NOT NULL, country VARCHAR(100) NOT NULL);";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement createStatement = conn.createStatement()) {
             createStatement.executeUpdate(createSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void save(Airport entity) {
        String query = "INSERT INTO airports (airportName, airportCode, city, country) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, entity.getAirportName());
            stmt.setString(2, entity.getAirportCode());
            stmt.setString(3, entity.getCity());
            stmt.setString(4, entity.getCountry());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Airport findById(int id) {
        String query = "SELECT * FROM airports WHERE airportId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Airport(
                        rs.getInt("airportId"),
                        rs.getString("airportName"),
                        rs.getString("airportCode"),
                        rs.getString("city"),
                        rs.getString("country")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Airport> findAll() {
        List<Airport> airports = new ArrayList<>();
        String query = "SELECT * FROM airports";

        try (Connection conn = DatabaseConnection.getPostgresConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                airports.add(new Airport(
                        rs.getInt("airportId"),
                        rs.getString("airportName"),
                        rs.getString("airportCode"),
                        rs.getString("city"),
                        rs.getString("country")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return airports;
    }

    @Override
    public void update(Airport entity) {
        String query = "UPDATE airports SET airportName = ?, airportCode = ? WHERE airportId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, entity.getAirportName());
            stmt.setString(2, entity.getAirportCode());
            stmt.setInt(3, entity.getAirportId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM airports WHERE airportId = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

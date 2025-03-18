package repository;

import domain.Airline;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirlineRepository implements CrudRepository<Airline> {
    public Connection conn;

    public AirlineRepository(Connection conn) {
        this.conn = conn;
        createTable();
    }

    private void createTable() {
        String createSQL = "CREATE TABLE IF NOT EXISTS airlines (airlineId SERIAL PRIMARY KEY, airlineName VARCHAR(100) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, phone VARCHAR(15) UNIQUE NOT NULL);";

        try (Statement createStatement = conn.createStatement()) {
             createStatement.executeUpdate(createSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Airline entity) {
        String query = "INSERT INTO airlines (airlineName, email, phone) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getAirlineName());
            stmt.setString(2, entity.getEmail());
            stmt.setString(3, entity.getPhone());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Airline findById(int id) {
        String query = "SELECT * FROM airlines WHERE airlineId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Airline(
                        rs.getInt("airlineId"),
                        rs.getString("airlineName"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Airline> findAll() {
        List<Airline> airlines = new ArrayList<>();
        String query = "SELECT * FROM airlines";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                airlines.add(new Airline(
                        rs.getInt("airlineId"),
                        rs.getString("airlineName"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return airlines;
    }

    @Override
    public void update(Airline entity) {
        String query = "UPDATE airlines SET airlineName = ?, email = ?, phone = ? WHERE airlineId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getAirlineName());
            stmt.setString(2, entity.getEmail());
            stmt.setString(3, entity.getPhone());
            stmt.setInt(4, entity.getAirlineId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM airlines WHERE airlineId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

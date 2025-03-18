package repository;

import database.DatabaseConnection;
import domain.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerRepository implements CrudRepository<Passenger> {
    private Connection conn;

    public PassengerRepository() {
        conn = DatabaseConnection.getMySQLConnection();
        createTable();
    }

    private void createTable() {
        String createSQL = "CREATE TABLE IF NOT EXISTS passengers (passengerId INT PRIMARY KEY AUTO_INCREMENT, firstName VARCHAR(100) NOT NULL, lastName VARCHAR(100) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, birthDate DATE NOT NULL, city VARCHAR(100) NOT NULL, country VARCHAR(100) NOT NULL);";

        try (Statement createStatement = conn.createStatement()) {
            createStatement.executeUpdate(createSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Passenger entity) {
        String query = "INSERT INTO passengers (firstName, lastName, email, birthDate, city, country) VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getFirstName());
            stmt.setString(2, entity.getLastName());
            stmt.setString(3, entity.getEmail());
            stmt.setDate(4, Date.valueOf(entity.getBirthDate()));
            stmt.setString(5, entity.getCity());
            stmt.setString(6, entity.getCountry());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Passenger findById(int id) {
        String query = "SELECT * FROM passengers WHERE passengerId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Passenger(
                        rs.getInt("passengerId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getDate("birthDate").toLocalDate(),
                        rs.getString("city"),
                        rs.getString("country")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Passenger> findAll() {
        String query = "SELECT * FROM passengers";
        List<Passenger> passengers = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                passengers.add(new Passenger(
                        rs.getInt("passengerId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getDate("birthDate").toLocalDate(),
                        rs.getString("city"),
                        rs.getString("country")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return passengers;
    }

    @Override
    public void update(Passenger entity) {
        String query = "UPDATE passengers SET firstName = ?, lastName = ?, email = ?, birthDate = ?, city = ?, country = ? WHERE passengerId = ?";

        try(PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getFirstName());
            stmt.setString(2, entity.getLastName());
            stmt.setString(3, entity.getEmail());
            stmt.setDate(4, Date.valueOf(entity.getBirthDate()));
            stmt.setString(5, entity.getCity());
            stmt.setString(6, entity.getCountry());
            stmt.setInt(7, entity.getPassengerId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM passengers WHERE passengerId = ?";

        try(PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package repository;

import domain.Airport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirportRepository implements CrudRepository<Airport> {
    private final Connection postgresConn;
    private final Connection myConn;

    public AirportRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTable();
    }

    private void createTable() {
        String createPostgreSQL = """
            CREATE TABLE IF NOT EXISTS airports (
            airportId SERIAL PRIMARY KEY,
            airportName VARCHAR(100) NOT NULL,
            airportCode VARCHAR(5) UNIQUE NOT NULL,
            city VARCHAR(100) NOT NULL,
            country VARCHAR(100) NOT NULL,
            version INTEGER DEFAULT 1);""";
        String createMySQL = """
            CREATE TABLE IF NOT EXISTS airports (
            airportId INTEGER PRIMARY KEY AUTO_INCREMENT,
            airportName VARCHAR(100) NOT NULL,
            airportCode VARCHAR(5) UNIQUE NOT NULL,
            city VARCHAR(100) NOT NULL,
            country VARCHAR(100) NOT NULL,
            version INTEGER DEFAULT 1);""";

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
    public void save(Airport entity) {
        String query = "INSERT INTO airports (airportName, airportCode, city, country, version) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement myStmt = myConn.prepareStatement(query);

            postgresStmt.setString(1, entity.getAirportName());
            postgresStmt.setString(2, entity.getAirportCode());
            postgresStmt.setString(3, entity.getCity());
            postgresStmt.setString(4, entity.getCountry());
            postgresStmt.setInt(5, 1);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            myStmt.setString(1, entity.getAirportName());
            myStmt.setString(2, entity.getAirportCode());
            myStmt.setString(3, entity.getCity());
            myStmt.setString(4, entity.getCountry());
            myStmt.setInt(5, 1);
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
    public Airport findById(int id) {
        String query = "SELECT * FROM airports WHERE airportId = ?";
        Airport airport = null;

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                airport = new Airport(
                        rs.getInt("airportId"),
                        rs.getString("airportName"),
                        rs.getString("airportCode"),
                        rs.getString("city"),
                        rs.getString("country")
                );
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return airport;
    }

    @Override
    public List<Airport> findAll() {
        List<Airport> airports = new ArrayList<>();
        String query = "SELECT * FROM airports";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                airports.add(new Airport(
                        rs.getInt("airportId"),
                        rs.getString("airportName"),
                        rs.getString("airportCode"),
                        rs.getString("city"),
                        rs.getString("country")
                ));
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return airports;
    }

    @Override
    public void update(Airport entity) {
        String select = "SELECT * FROM airports WHERE airportId = ? FOR UPDATE";
        String query = """
            UPDATE airports
            SET airportName = ?, airportCode = ?, version = version + 1
            WHERE airportId = ? AND version = ?
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getAirportId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setString(1, entity.getAirportName());
                postgresStmt.setString(2, entity.getAirportCode());
                postgresStmt.setInt(3, entity.getAirportId());
                postgresStmt.setInt(4, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getAirportId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setString(1, entity.getAirportName());
                myStmt.setString(2, entity.getAirportCode());
                myStmt.setInt(3, entity.getAirportId());
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
        String query = "DELETE FROM airports WHERE airportId = ?";

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

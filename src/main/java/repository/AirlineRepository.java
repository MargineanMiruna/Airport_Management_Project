package repository;

import domain.Airline;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirlineRepository implements CrudRepository<Airline> {
    private final Connection postgresConn;
    private final Connection myConn;

    public AirlineRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTable();
    }

    private void createTable() {
        String createPostgreSQL = """
            CREATE TABLE IF NOT EXISTS airlines (
            airlineId SERIAL PRIMARY KEY,
            airlineName VARCHAR(100) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            phone VARCHAR(15) UNIQUE NOT NULL,
            version INTEGER DEFAULT 1);""";
        String createMySQL = """
            CREATE TABLE IF NOT EXISTS airlines (
            airlineId INTEGER PRIMARY KEY AUTO_INCREMENT,
            airlineName VARCHAR(100) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            phone VARCHAR(15) UNIQUE NOT NULL,
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
    public void save(Airline entity) {
        String query = "INSERT INTO airlines (airlineName, email, phone, version) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement myStmt = myConn.prepareStatement(query);

            postgresStmt.setString(1, entity.getAirlineName());
            postgresStmt.setString(2, entity.getEmail());
            postgresStmt.setString(3, entity.getPhone());
            postgresStmt.setInt(4, 1);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            myStmt.setString(1, entity.getAirlineName());
            myStmt.setString(2, entity.getEmail());
            myStmt.setString(3, entity.getPhone());
            myStmt.setInt(4, 1);
            myStmt.executeUpdate();
            myConn.commit();

        } catch (SQLException e) {
            try {
                postgresConn.rollback();
                myConn.rollback();
                System.out.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackException) {
                System.out.println("Rollback failed: " + rollbackException.getMessage());            }
        }
    }

    @Override
    public Airline findById(int id) {
        String query = "SELECT * FROM airlines WHERE airlineId = ?";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
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

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Airline> findAll() {
        List<Airline> airlines = new ArrayList<>();
        String query = "SELECT * FROM airlines";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                airlines.add(new Airline(
                        rs.getInt("airlineId"),
                        rs.getString("airlineName"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return airlines;
    }

    @Override
    public void update(Airline entity) {
        String select = "SELECT * FROM airlines WHERE airlineId = ? FOR UPDATE";
        String query = """
            UPDATE airlines
            SET airlineName = ?, email = ?, phone = ?, version = version + 1
            WHERE airlineId = ? AND version = ?
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getAirlineId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setString(1, entity.getAirlineName());
                postgresStmt.setString(2, entity.getEmail());
                postgresStmt.setString(3, entity.getPhone());
                postgresStmt.setInt(4, entity.getAirlineId());
                postgresStmt.setInt(5, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getAirlineId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setString(1, entity.getAirlineName());
                myStmt.setString(2, entity.getEmail());
                myStmt.setString(3, entity.getPhone());
                myStmt.setInt(4, entity.getAirlineId());
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

    @Override
    public void delete(int id) {
        String query = "DELETE FROM airlines WHERE airlineId = ?";

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

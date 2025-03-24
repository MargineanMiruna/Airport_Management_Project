package repository;

import domain.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerRepository implements CrudRepository<Passenger> {
    private final Connection postgresConn;
    private final Connection myConn;

    public PassengerRepository(Connection postgresConn, Connection myConn) {
        this.postgresConn = postgresConn;
        this.myConn = myConn;
        createTable();
    }

    private void createTable() {
        String createPostgreSQL = """
            CREATE TABLE IF NOT EXISTS passengers (
            passengerId SERIAL PRIMARY KEY,
            firstName VARCHAR(100) NOT NULL,
            lastName VARCHAR(100) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            birthDate DATE NOT NULL,
            city VARCHAR(100) NOT NULL,
            country VARCHAR(100) NOT NULL,
            version INTEGER DEFAULT 1);""";
        String createMySQL = """
            CREATE TABLE IF NOT EXISTS passengers (
            passengerId INTEGER PRIMARY KEY AUTO_INCREMENT,
            firstName VARCHAR(100) NOT NULL,
            lastName VARCHAR(100) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            birthDate DATE NOT NULL,
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
    public void save(Passenger entity) {
        String query = "INSERT INTO passengers (firstName, lastName, email, birthDate, city, country, version) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try{
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            PreparedStatement myStmt = myConn.prepareStatement(query);

            postgresStmt.setString(1, entity.getFirstName());
            postgresStmt.setString(2, entity.getLastName());
            postgresStmt.setString(3, entity.getEmail());
            postgresStmt.setDate(4, Date.valueOf(entity.getBirthDate()));
            postgresStmt.setString(5, entity.getCity());
            postgresStmt.setString(6, entity.getCountry());
            postgresStmt.setInt(7, 1);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            myStmt.setString(1, entity.getFirstName());
            myStmt.setString(2, entity.getLastName());
            myStmt.setString(3, entity.getEmail());
            myStmt.setDate(4, Date.valueOf(entity.getBirthDate()));
            myStmt.setString(5, entity.getCity());
            myStmt.setString(6, entity.getCountry());
            myStmt.setInt(7, 1);
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
    public Passenger findById(int id) {
        String query = "SELECT * FROM passengers WHERE passengerId = ?";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
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

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    public Passenger findByEmail(String email) {
        String query = "SELECT * FROM passengers WHERE email = ?";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            PreparedStatement stmt = postgresConn.prepareStatement(query);
            stmt.setString(1, email);
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

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Passenger> findAll() {
        List<Passenger> passengers = new ArrayList<>();
        String query = "SELECT * FROM passengers";

        try {
            postgresConn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Statement stmt = postgresConn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

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

            postgresConn.commit();

        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        return passengers;
    }

    @Override
    public void update(Passenger entity) {
        String select = "SELECT * FROM passengers WHERE passengerId = ? FOR UPDATE";
        String query = """
            UPDATE passengers
            SET firstName = ?, lastName = ?, email = ?, birthDate = ?, city = ?, country = ?, version = version + 1
            WHERE passengerId = ? AND version = ?
        """;

        try {
            PreparedStatement postgresSelectStmt = postgresConn.prepareStatement(select);
            postgresSelectStmt.setInt(1, entity.getPassengerId());
            ResultSet prs = postgresSelectStmt.executeQuery();

            if (prs.next()) {
                PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
                postgresStmt.setString(1, entity.getFirstName());
                postgresStmt.setString(2, entity.getLastName());
                postgresStmt.setString(3, entity.getEmail());
                postgresStmt.setDate(4, Date.valueOf(entity.getBirthDate()));
                postgresStmt.setString(5, entity.getCity());
                postgresStmt.setString(6, entity.getCountry());
                postgresStmt.setInt(7, entity.getPassengerId());
                postgresStmt.setInt(8, prs.getInt("version"));
                postgresStmt.executeUpdate();
            }

            PreparedStatement mySelectStmt = myConn.prepareStatement(select);
            mySelectStmt.setInt(1, entity.getPassengerId());
            ResultSet mrs = mySelectStmt.executeQuery();

            if (mrs.next()) {
                PreparedStatement myStmt = myConn.prepareStatement(query);
                myStmt.setString(1, entity.getFirstName());
                myStmt.setString(2, entity.getLastName());
                myStmt.setString(3, entity.getEmail());
                myStmt.setDate(4, Date.valueOf(entity.getBirthDate()));
                myStmt.setString(5, entity.getCity());
                myStmt.setString(6, entity.getCountry());
                myStmt.setInt(7, entity.getPassengerId());
                myStmt.setInt(8, mrs.getInt("version"));
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
        String query = "DELETE FROM passengers WHERE passengerId = ?";

        try {
            PreparedStatement postgresStmt = postgresConn.prepareStatement(query);
            postgresStmt.setInt(1, id);
            postgresStmt.executeUpdate();
            postgresConn.commit();

            PreparedStatement myStmt = myConn.prepareStatement(query);
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

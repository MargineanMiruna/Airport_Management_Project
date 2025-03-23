package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/Flights_Management";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "Calorifer_123";

    private static final String MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/Bookings_Management";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "Calorifer_123";

    static {
        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load database drivers.");
        }
    }

    public static Connection getPostgresConnection() {
        try {
            return DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to PostgreSQL.");
        }
    }

    public static Connection getMySQLConnection() {
        try {
            return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to MySQL.");
        }
    }
}

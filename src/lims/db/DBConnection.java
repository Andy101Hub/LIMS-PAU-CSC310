package lims.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/lims_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "kiitan123";

    // This method creates and returns a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // This method is only for testing if the database connection works
    public static void testConnection() {
        try {
            Connection conn = getConnection();

            if (conn != null) {
                System.out.println("Database connected successfully!");
                conn.close();
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }

    // Temporary main method for testing this file directly
    public static void main(String[] args) {
        testConnection();
    }
}
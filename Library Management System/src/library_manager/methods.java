package library_manager;

import java.sql.*;

public class methods {

    // Check if a string is numeric
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            System.out.println("Received null or empty string, returning false.");
            return false; // Return false if the string is null or empty
        }

        // Iterate through each character to check if it's a digit
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                System.out.println("String contains non-numeric character: " + c + ", returning false.");
                return false; // Return false if any character is not a digit
            }
        }

        System.out.println("String is numeric: " + str);
        return true; // Return true if all characters are digits
    }
}

class DatabaseUtil {

    public static boolean checkDatabaseExists(Connection connection, String databaseName) throws SQLException {
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            System.out.println("Checking if database exists: " + databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next(); // True if the database exists
                if (exists) {
                    System.out.println("Database exists: " + databaseName);
                } else {
                    System.out.println("Database does not exist: " + databaseName);
                }
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking database existence: " + e.getMessage());
            throw e;
        }
    }

    public static boolean checkTableExists(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, tableName);
            System.out.println("Checking if table exists: " + tableName + " in database: " + databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next(); // True if the table exists
                if (exists) {
                    System.out.println("Table exists: " + tableName);
                } else {
                    System.out.println("Table does not exist: " + tableName);
                }
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            throw e;
        }
    }
}


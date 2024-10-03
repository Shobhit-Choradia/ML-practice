package library_manager;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLConnection {
    private static final Logger logger = Logger.getLogger(SQLConnection.class.getName());
    private static final String CONFIG_FILE = "config.properties"; // Configuration file path
    private static String baseUrl;
    private static String username;
    private static String password;
    private static Connection connection = null;

    private SQLConnection() {}

    // Method to load properties from the config file or perform first-time setup
    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            // If the config file exists, load the credentials
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                baseUrl = properties.getProperty("db.url");
                username = properties.getProperty("db.username");
                password = properties.getProperty("db.password");
                logger.info("Loaded credentials from config file.");
            }
        } else {
            // If config file doesn't exist, perform first-time setup
            logger.warning("Configuration file not found. Starting first-time setup.");
            firstTimeSetup(properties, configFile);
        }
    }

    // First-time setup method to prompt user and save credentials
    private static void firstTimeSetup(Properties properties, File configFile) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("First-time setup: Please enter the database credentials.");
            System.out.print("Database URL (e.g., jdbc:mysql://localhost:3306/): ");
            baseUrl = sc.nextLine();
            System.out.print("Username: ");
            username = sc.nextLine();
            System.out.print("Password: ");
            password = sc.nextLine();

            // Store the credentials in the properties object
            properties.setProperty("db.url", baseUrl);
            properties.setProperty("db.username", username);
            properties.setProperty("db.password", password);

            // Save the properties to the config file
            try (OutputStream output = new FileOutputStream(configFile)) {
                properties.store(output, "Database Credentials");
                logger.info("Credentials saved successfully. Setup complete.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save configuration file.", e);
            }
        }
    }

    // Method to get a connection using the loaded or newly created properties
    public static Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            if (baseUrl == null || username == null || password == null) {
                loadProperties(); // Load or set up properties on first access
            }
            try {
                connection = DriverManager.getConnection(baseUrl, username, password);
                logger.info("Connection established successfully.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to establish connection.", e);
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Connection closed successfully.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to close connection.", e);
            }
        } else {
            logger.warning("Attempted to close a connection that is null.");
        }
    }

    public static void updateBaseUrl() throws IOException {
        String databaseName = "Library_Manager";
        if (baseUrl == null) {
            loadProperties(); // Load properties if baseUrl is not already set
        }

        // Check if the URL already contains the database name
        if (!baseUrl.endsWith("/" + databaseName)) {
            baseUrl += databaseName; // Append the database name to the URL
            logger.info("Updated base URL to include database: " + baseUrl);

            // Create a properties object to save the updated URL
            Properties properties = new Properties();
            File configFile = new File(CONFIG_FILE);

            // Load existing properties from the config file
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    properties.load(input);
                }
            } else {
                logger.warning("Configuration file does not exist. It will be created during first-time setup.");
            }

            properties.setProperty("db.url", baseUrl); // Update the URL in properties

            // Save the properties back to the same config file
            try (OutputStream output = new FileOutputStream(configFile)) {
                properties.store(output, "Updated Database Credentials");
                logger.info("Base URL with database saved in config file.");
            }
        } else {
            logger.info("Base URL already contains the database name.");
        }
    }
}


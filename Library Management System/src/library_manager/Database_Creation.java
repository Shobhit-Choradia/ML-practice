package library_manager;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

class Database_Creation
{
    private static final Logger logger = LibraryLogger.getLogger();
    public static void main(String[] args) throws SQLException, IOException {
        Connection connection = SQLConnection.getConnection();
        Statement statement = null;
        logger.info("Checking database setup...");
        try
        {
            statement = connection.createStatement();
            String databaseName = "Library_Manager";


            if (!DatabaseUtil.checkDatabaseExists(connection, databaseName))
            {
                logger.info("Database dosen't exist...");
                statement.execute("CREATE DATABASE " + databaseName);
                logger.info("Database " + databaseName + " created successfully.");
                statement.close();
                connection.close();
                SQLConnection.updateBaseUrl();
            }
            else
            {
                logger.info("Database " + databaseName + " already exists.");
            }


            connection = SQLConnection.getConnection();
            statement = connection.createStatement();
            logger.info("Starting to check for tables...");
            // Create Books table
            createTableIfNotExists(connection, statement, databaseName, "Books",
                    "CREATE TABLE Books ( " +
                            "BOOK_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ISBN CHAR(13) NOT NULL UNIQUE, " +   // Added UNIQUE constraint to ISBN
                            "NAME VARCHAR(255) NOT NULL, " +
                            "AUTHOR VARCHAR(255) NOT NULL, " +
                            "PUBLICATION VARCHAR(255) NOT NULL, " +
                            "GENRE VARCHAR(255), " +              // Nullable genre
                            "LANGUAGE VARCHAR(255), " +           // Added Language
                            "DESCRIPTION TEXT, " +               // Added description
                            "PUBLICATION_YEAR INT, " +            // Added publication year
                            "PAGES INT, " +                       // Added pages
                            "BOOK_TYPE VARCHAR(255))");

            // Create Copies table
            createTableIfNotExists(connection, statement, databaseName, "Copies",
                    "CREATE TABLE Copies ( " +
                            "BOOK_ID INT NOT NULL, " +                             // Reference to the book
                            "AVAILABILITY TINYINT(1) DEFAULT 1, " +        // Status: 1 = available, 0 = checked out
                            "CURRENT_COPIES INT NOT NULL DEFAULT 1, " +           // Current number of copies available
                            "TOTAL_COPIES INT NOT NULL DEFAULT 1, " +             // Total number of copies for this book
                            "FOREIGN KEY (BOOK_ID) REFERENCES Books (BOOK_ID) ON DELETE CASCADE, " + // Ensures deletion of copies when a book is deleted
                            "CHECK (CURRENT_COPIES <= TOTAL_COPIES) " +           // Ensures CURRENT_COPIES does not exceed TOTAL_COPIES
                            ")");
            // Create Members table
            createTableIfNotExists(connection, statement, databaseName, "Members",
                    "CREATE TABLE Members ( " +
                            "MEMBER_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                            "NAME VARCHAR(255) NOT NULL, " +
                            "MOBILE_NO CHAR(10) NOT NULL, " +
                            "EMAIL_ADDRESS VARCHAR(255) NOT NULL, " +
                            "SUBSCRIPTION VARCHAR(255), " +      // Nullable subscription
                            "MEMBERSHIP_START_DATE DATE NOT NULL, " +  // Added membership start date
                            "MEMBERSHIP_END_DATE DATE, " +           // Added membership end date
                            "BORROW_LIMIT INT DEFAULT 5)");  // Added borrow limit

            // Create Librarians table
            createTableIfNotExists(connection, statement, databaseName, "Librarians",
                    "CREATE TABLE Librarians ( " +
                            "LIBRARIAN_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                            "NAME VARCHAR(255) NOT NULL, " +
                            "MOBILE_NO CHAR(10) NOT NULL, " +
                            "EMAIL_ADDRESS VARCHAR(255) NOT NULL, " +
                            "USERNAME VARCHAR(255) NOT NULL, " +
                            "PASSWORD VARCHAR(255) NOT NULL, " +
                            "ROLE VARCHAR(50))");  // Added role for librarian permissions

            // Create Book_Issues table
            createTableIfNotExists(connection, statement, databaseName, "Book_Issues",
                    "CREATE TABLE Book_Issues ( " +
                            "ISSUE_ID INT AUTO_INCREMENT PRIMARY KEY, " +   // Auto-increment ID for issues
                            "BOOK_ID INT NOT NULL, " +
                            "MEMBER_ID INT NOT NULL, " +
                            "ISSUE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                            "RETURN_DATE DATETIME NOT NULL, " +
                            "ACTUAL_RETURN_DATE DATETIME, " +     // Added actual return date
                            "FINE FLOAT DEFAULT 0, " +            // Added fine column
                            "FOREIGN KEY (BOOK_ID) REFERENCES Copies (BOOK_ID), " +
                            "FOREIGN KEY (MEMBER_ID) REFERENCES Members (MEMBER_ID))");

        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error occurred during database setup", e);
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private static void createTableIfNotExists(Connection connection, Statement statement, String databaseName, String tableName, String createSQL)
    {
        try {
            if (!DatabaseUtil.checkTableExists(connection, databaseName, tableName))
            {
                statement.execute(createSQL);
                System.out.println("Table " + tableName + " created successfully.");
                if(tableName.equals("Copies"))
                {
                    String createTriggerSql = "CREATE TRIGGER UpdateAvailability " +
                            "AFTER INSERT OR UPDATE ON Copies " +
                            "FOR EACH ROW " +
                            "BEGIN " +
                            "IF NEW.CURRENT_COPIES = 0 THEN " +
                            "SET NEW.AVAILABILITY = 0; " +
                            "ELSE " +
                            "SET NEW.AVAILABILITY = 1; " +
                            "END IF; " +
                            "END;";
                    statement.execute(createTriggerSql);
                    logger.info("Trigger for table Copies created");
                }
                logger.info("Created table " + tableName);
            }
            else
            {
                System.out.println("Table " + tableName + " already exists.");
                logger.info("Table " + tableName + " Exists");
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error occured while creating table " + tableName, e);
        }
    }
}


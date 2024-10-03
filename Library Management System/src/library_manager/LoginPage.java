package library_manager;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class LoginPage {
    private static final Logger logger = LibraryLogger.getLogger();

    public static void main(String[] args) {
        int choice = 0;
        Scanner sc = new Scanner(System.in);
        logger.info("Library Manager application started.");

        System.out.println("Welcome to the Library Manager.");

        while (choice != 3) {
            System.out.println("""
                    Type 1 to create a new account.
                    Type 2 to login.
                    Type 3 to exit.
                    """);

            try {
                choice = sc.nextInt();
                sc.nextLine(); // Clear the newline after nextInt
                if (choice < 1 || choice > 3) {
                    logger.warning("Invalid menu choice: " + choice);
                    System.out.println("Please enter a valid option.");
                    continue;
                }
            } catch (InputMismatchException e) {
                logger.warning("InputMismatchException occurred while selecting menu: " + e.getMessage());
                System.out.println("Invalid input. Please enter a number between 1 and 3.");
                sc.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1 -> newAccount(sc);
                case 2 -> login(sc);
                case 3 -> {
                    System.out.println("Exiting...");
                    logger.info("Exiting Library Manager.");
                    System.exit(0);
                }
            }
        }
    }

    private static void newAccount(Scanner sc) {
        logger.info("New account creation initiated.");
        System.out.println("To create a new account, please enter the user ID and password of an existing account owner.");
        System.out.print("User ID - ");
        String userId = sc.next();
        System.out.print("User Password - ");
        String userPass = sc.next();

        if (authenticate(userId, userPass)) {
            logger.info("Successful authentication by user: " + userId);
            System.out.println("\nPrevious user has been verified. Enter new user details:\n");

            System.out.print("Name - ");
            String name = sc.next();
            System.out.print("Mobile Number - ");
            String mobileNo = sc.next();
            System.out.print("Email ID - ");
            String emailId = sc.next();
            System.out.print("User ID - ");
            userId = sc.next();
            System.out.print("Password - ");
            userPass = sc.next();

            if (addLibrarian(name, mobileNo, emailId, userId, userPass)) {
                logger.info("New librarian added: " + userId);
                System.out.println("New librarian has been added to the database.");
            } else {
                logger.severe("Failed to add a new librarian: " + userId);
                System.out.println("Error occurred while adding a new librarian.");
            }
        } else {
            logger.warning("Authentication failed for user: " + userId);
            System.out.println("Authentication failed. Please try again.");
        }
    }

    private static void login(Scanner sc) {
        logger.info("Login attempt started.");
        System.out.println("Login Page:");

        while (true) {
            System.out.print("User ID - ");
            String userId = sc.next();
            System.out.print("User Password - ");
            String userPass = sc.next();

            if (authenticate(userId, userPass)) {
                logger.info("User " + userId + " logged in successfully.");
                System.out.println("Logged in successfully.");
                MainPage.main(new String[]{"Welcome to the Main Page!"});
                System.exit(0);
            } else {
                logger.warning("Failed login attempt for user: " + userId);
                System.out.println("Invalid User ID or password. Please try again.");
            }
        }
    }

    private static boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM Librarians WHERE USERNAME = ? AND PASSWORD = ?";
        logger.info("Authenticating user: " + username);

        try (Connection connection = SQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean authenticated = resultSet.next();
                if (authenticated) {
                    logger.info("Authentication successful for user: " + username);
                } else {
                    logger.warning("Authentication failed for user: " + username);
                }
                return authenticated;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error occurred during authentication for user: " + username, e);
            System.out.println("Authentication failed due to a database error: \n" + e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException occurred during authentication for user: " + username, e);
            System.out.println("Authentication failed due to IOException: \n" + e.getMessage());
        }
        return false;
    }

    private static boolean addLibrarian(String name, String mobileNo, String emailId, String userId, String userPass) {
        String sql = "INSERT INTO Librarians (NAME, MOBILE_NO, EMAIL_ADDRESS, USERNAME, PASSWORD) VALUES (?, ?, ?, ?, ?)";
        logger.info("Attempting to add a new librarian: " + userId);

        try (Connection connection = SQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, mobileNo);
            preparedStatement.setString(3, emailId);
            preparedStatement.setString(4, userId);
            preparedStatement.setString(5, userPass);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Librarian added successfully: " + userId);
                return true;
            } else {
                logger.warning("Failed to add librarian: " + userId);
            }

        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, "Error occurred while adding librarian: " + userId, e);
            System.out.println("Error adding librarian: " + e.getMessage());
        }
        return false;
    }
}



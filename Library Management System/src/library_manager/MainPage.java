package library_manager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainPage
{
    static Scanner sc = new Scanner(System.in);
    private static final Logger logger = Logger.getLogger(MainPage.class.getName());

    public static void main(String[] args)
    {
        logger.info("Starting Library Management System");

        try (Connection connection = SQLConnection.getConnection())
        {
            // Display initial statistics
            logger.info("Displaying initial statistics...");
            displayStatistics(connection);
        }
        catch (SQLException | IOException e)
        {
            logger.log(Level.SEVERE, "Error during startup", e);
        }

        int choice = 0;

        while (choice != 8)
        {
            System.out.println("\n\n");
            System.out.println("""
                    Type 1 to add new book to database.    Type 5 to update a book's details.
                    Type 2 to remove book from database.   Type 6 to add new member.
                    Type 3 to issue book.                  Type 7 to delete member.
                    Type 4 to return book.                 Type 8 to exit.
                    """);

            try
            {
                choice = sc.nextInt();
                sc.nextLine(); // Clear newline

                if (choice < 1 || choice > 8)
                {
                    logger.warning("Invalid option selected by the user: " + choice);
                    System.out.println("Please enter a valid option.");
                    continue;
                }

                logger.info("User selected option: " + choice);
            }
            catch (InputMismatchException e)
            {
                logger.warning("Invalid input format for option. Expected a number.");
                System.out.println("Invalid input. Please enter a number between 1 and 8.");
                sc.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice)
            {
                case 1 -> addBook();
                case 2 -> removeBook();
                case 3 -> issueBook();
                case 4 -> returnBook();
                case 5 -> updateBookDetails();
                case 6 -> registerMember();
                case 7 -> deleteMember();
                case 8 -> {
                    logger.info("User exiting the system.");
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            }
        }
    }

    private static void displayStatistics(Connection connection) throws SQLException
    {
        logger.info("Fetching statistics from the database...");
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Books;");
        rs.next();
        System.out.println("Total books: " + rs.getInt(1));
        logger.info("Total books: " + rs.getInt(1));

        rs = statement.executeQuery("SELECT COUNT(*) FROM Members;");
        rs.next();
        System.out.println("Total members: " + rs.getInt(1));
        logger.info("Total members: " + rs.getInt(1));

        rs = statement.executeQuery("SELECT COUNT(*) FROM Book_Issues;");
        rs.next();
        System.out.println("Books currently issued: " + rs.getInt(1));
        logger.info("Books currently issued: " + rs.getInt(1));

        rs = statement.executeQuery("SELECT COUNT(*) FROM Book_Issues WHERE RETURN_DATE < NOW();");
        rs.next();
        System.out.println("Overdue books: " + rs.getInt(1));
        logger.info("Overdue books: " + rs.getInt(1));

        statement.close();
    }

    public static void addBook() {
        logger.info("Initiating process to add a new book.");
        System.out.println("Enter Book details - ");
        System.out.print("ISBN number - ");
        String ISBN = sc.nextLine();
        System.out.print("Book name - ");
        String NAME = sc.nextLine();
        System.out.print("Author's name - ");
        String AUTHOR = sc.nextLine();
        System.out.print("Publication - ");
        String PUBLICATION = sc.nextLine();
        System.out.print("Genre - ");
        String GENRE = sc.nextLine();
        System.out.print("Language - ");
        String LANGUAGE = sc.nextLine();
        System.out.print("Number of copies - ");
        int COPIES = sc.nextInt();
        sc.nextLine(); // Clear newline
        System.out.print("Type of book - ");
        String TYPE = sc.nextLine();
        System.out.print("Description - ");
        String DESCRIPTION = sc.nextLine();

        System.out.print("Category ID (if known) - ");
        int CATEGORY_ID = sc.nextInt();
        sc.nextLine();

        logger.info("Book details entered: ISBN=" + ISBN + ", Name=" + NAME);

        try (Connection connection = SQLConnection.getConnection()) {
            String sql = """
                    INSERT INTO Books (ISBN, NAME, AUTHOR, PUBLICATION, GENRE, LANGUAGE, COPIES, BOOK_TYPE, DESCRIPTION, CATEGORY_ID)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, ISBN);
            statement.setString(2, NAME);
            statement.setString(3, AUTHOR);
            statement.setString(4, PUBLICATION);
            statement.setString(5, GENRE);
            statement.setString(6, LANGUAGE);
            statement.setInt(7, COPIES);
            statement.setString(8, TYPE);
            statement.setString(9, DESCRIPTION);
            statement.setInt(10, CATEGORY_ID);

            statement.executeUpdate();
            logger.info("Book added successfully: " + NAME);
            System.out.println("Book added successfully!");
        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, "Failed to add book: " + NAME, e);
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void removeBook() {
        logger.info("Initiating process to remove a book.");
        HashMap<String, String> conditions = new HashMap<>();
        String condition;

        while (true) {
            System.out.print("Enter condition in format 'column:value' or type 'done' to finish: ");
            condition = sc.nextLine();

            if (condition.equalsIgnoreCase("done")) {
                break;
            }

            String[] pair = condition.split(":");
            if (pair.length != 2) {
                System.out.println("Invalid format. Please use 'column:value'.");
                logger.warning("Invalid condition format entered.");
                continue;
            }

            String key = pair[0].trim();
            String value = pair[1].trim();
            conditions.put(key, value);
        }

        if (conditions.isEmpty()) {
            logger.warning("No conditions provided for removing book.");
            return;
        }

        StringBuilder query = new StringBuilder("DELETE FROM Books WHERE ");
        for (String column : conditions.keySet()) {
            query.append(column).append(" = ? AND ");
        }
        query.setLength(query.length() - 5); // Remove last " AND "

        logger.info("Delete query built with conditions: " + conditions);

        try (Connection connection = SQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
            int index = 1;
            for (String value : conditions.values()) {
                preparedStatement.setString(index++, value);
            }

            int rowsAffected = preparedStatement.executeUpdate();
            logger.info(rowsAffected + " records deleted.");

            if (rowsAffected > 0) {
                System.out.println("Records deleted successfully.");
            } else {
                System.out.println("No records found with the given conditions.");
            }

        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, "Failed to remove book.", e);
            System.out.println("Error : " + e.getMessage());
        }
    }

    // Similarly, add logger messages to the other methods like issueBook, returnBook, updateBookDetails, etc.

    public static void issueBook() {
        logger.info("Initiating process to issue a book.");
        System.out.print("Enter member ID: ");
        int memberId = sc.nextInt();
        System.out.print("Enter book ID of the book: ");
        int bookId = sc.nextInt();
        if(!isBookAvailable(bookId)){
            logger.warning("Book with ID " + bookId + " is not available.");
            System.out.println("Book currently not available");
            return;
        }

        if (!searchMemberById(memberId)) {
            logger.warning("Invalid member ID: " + memberId);
            System.out.println("The customer is not a member. Please register the member first.");
            return;
        }

        issueBookToMember(bookId, memberId);
    }

    public static void returnBook() {
        logger.info("Entering returnBook method.");
        System.out.print("Enter member ID: ");
        int memberId = sc.nextInt();
        System.out.print("Enter copy ID of the book: ");
        int bookId = sc.nextInt();

        // Handle return book logic and calculate fine if necessary
        handleBookReturn(bookId, memberId);
    }

    private static void handleBookReturn(int bookId, int memberId) {
        logger.info("Handling book return for book ID: " + bookId + ", member ID: " + memberId);
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = """
                    SELECT RETURN_DATE, DATEDIFF(NOW(), RETURN_DATE) AS daysOverdue FROM Book_Issues
                    WHERE BOOK_ID = ? AND MEMBER_ID = ?""";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, bookId);
            statement.setInt(2, memberId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int daysOverdue = rs.getInt("daysOverdue");
                logger.info("Book overdue by " + daysOverdue + " days.");
                if (daysOverdue > 0) {
                    System.out.println("Book is overdue by " + daysOverdue + " days.");
                }

                removeBookIssueRecord(bookId, memberId);
                logger.info("Book returned successfully.");
                System.out.println("Book returned successfully.");
            } else {
                logger.warning("No book issue record found for book ID: " + bookId + ", member ID: " + memberId);
                System.out.println("No book issue record found.");
            }
        } catch (SQLException | IOException e) {
            logger.severe("Error while handling book return: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void registerMember() {
        logger.info("Entering registerMember method.");
        System.out.print("Enter member name: ");
        String name = sc.nextLine();
        System.out.print("Enter mobile number: ");
        String mobile = sc.nextLine();
        System.out.print("Enter email address: ");
        String email = sc.nextLine();
        System.out.print("Enter subscription type: ");
        String subscription = sc.nextLine();

        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "INSERT INTO Members (NAME, MOBILE_NO, EMAIL_ADDRESS, SUBSCRIPTION) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, mobile);
            statement.setString(3, email);
            statement.setString(4, subscription);

            statement.executeUpdate();
            logger.info("Member added successfully: " + name);
            System.out.println("Member added successfully.");
        } catch (SQLException | IOException e) {
            logger.severe("Error while adding member: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void deleteMember() {
        logger.info("Entering deleteMember method.");
        System.out.print("Enter member ID to delete: ");
        int memberId = sc.nextInt();
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "DELETE FROM Members WHERE MEMBER_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, memberId);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                logger.info("Member deleted successfully: ID " + memberId);
                System.out.println("Member deleted successfully.");
            } else {
                logger.warning("No member found with ID: " + memberId);
                System.out.println("No member found with the given ID.");
            }
        } catch (SQLException | IOException e) {
            logger.severe("Error while deleting member: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    private static void issueBookToMember(int bookId, int memberId) {
        logger.info("Entering issueBookToMember for book ID: " + bookId + " and member ID: " + memberId);
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "INSERT INTO Book_Issues (BOOK_ID, MEMBER_ID, ISSUE_DATE, RETURN_DATE) VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY))";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, bookId);
            statement.setInt(2, memberId);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("Book issued successfully: Book ID " + bookId + ", Member ID " + memberId);
                String updateCopiesSql = "UPDATE Copies SET CURRENT_COPIES = CURRENT_COPIES - 1 WHERE BOOK_ID = ?";
                PreparedStatement updateCopiesStatement = connection.prepareStatement(updateCopiesSql);
                updateCopiesStatement.setInt(1, bookId);
                int rowsUpdated = updateCopiesStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    logger.info("Updated current copies for book ID: " + bookId);
                    System.out.println("Book issued successfully and current copies updated.");
                } else {
                    logger.warning("Failed to update current copies after issuing book ID: " + bookId);
                    System.out.println("Failed to update current copies after issuing book.");
                }
            } else {
                logger.warning("Failed to issue book ID: " + bookId + " to member ID: " + memberId);
                System.out.println("Failed to issue book.");
            }
        } catch (SQLException | IOException e) {
            logger.severe("Error while issuing book: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    private static void removeBookIssueRecord(int bookId, int memberId) {
        logger.info("Removing book issue record for book ID: " + bookId + " and member ID: " + memberId);
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "DELETE FROM Book_Issues WHERE BOOK_ID = ? AND MEMBER_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, bookId);
            statement.setInt(2, memberId);
            statement.executeUpdate();
            logger.info("Book issue record deleted for book ID: " + bookId + " and member ID: " + memberId);
        } catch (SQLException | IOException e) {
            logger.severe("Error while removing book issue record: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void updateBookDetails() {
        logger.info("Entering updateBookDetails method.");
        Scanner sc = new Scanner(System.in);
        HashMap<String, String> conditions = new HashMap<>();
        HashMap<String, String> updates = new HashMap<>();
        String condition;

        while (true) {
            System.out.print("Enter condition in format 'column:value' or type 'done' to finish: ");
            condition = sc.nextLine();

            if (condition.equalsIgnoreCase("done")) {
                break;
            }

            String[] pair = condition.split(":");
            if (pair.length != 2) {
                logger.warning("Invalid format for condition: " + condition);
                System.out.println("Invalid format. Please use 'column:value'.");
                continue;
            }

            conditions.put(pair[0].trim(), pair[1].trim());
        }

        while (true) {
            System.out.print("Enter update in format 'column:value' or type 'done' to finish: ");
            condition = sc.nextLine();

            if (condition.equalsIgnoreCase("done")) {
                break;
            }

            String[] pair = condition.split(":");
            if (pair.length != 2) {
                logger.warning("Invalid format for update: " + condition);
                System.out.println("Invalid format. Please use 'column:value'.");
                continue;
            }

            updates.put(pair[0].trim(), pair[1].trim());
        }

        StringBuilder query = new StringBuilder("UPDATE Books SET ");

        for (String column : updates.keySet()) {
            query.append(column).append(" = ?, ");
        }

        if (!updates.isEmpty()) {
            query.setLength(query.length() - 2);
        } else {
            logger.warning("No updates provided.");
            System.out.println("No updates provided.");
            return;
        }

        query.append(" WHERE ");
        for (String column : conditions.keySet()) {
            query.append(column).append(" = ? AND ");
        }

        if (!conditions.isEmpty()) {
            query.setLength(query.length() - 5);
        } else {
            logger.warning("No conditions provided.");
            System.out.println("No conditions provided.");
            return;
        }

        try (Connection connection = SQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
            int index = 1;
            for (String value : updates.values()) {
                preparedStatement.setString(index++, value);
            }

            for (String value : conditions.values()) {
                preparedStatement.setString(index++, value);
            }

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Records updated successfully.");
                System.out.println("Records updated successfully.");
            } else {
                logger.warning("No records found with the given conditions.");
                System.out.println("No records found with the given conditions.");
            }

        } catch (SQLException | IOException e) {
            logger.severe("Error while updating book details: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
    }

    private static boolean isBookAvailable(int bookId) {
        logger.info("Checking if book ID: " + bookId + " is available.");
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "SELECT CURRENT_COPIES FROM Copies WHERE BOOK_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, bookId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int currentCopies = rs.getInt(1);
                if (currentCopies > 0) {
                    logger.info("Book is available: " + currentCopies + " copies left.");
                    return true;
                } else {
                    logger.info("Book is not available: No copies left.");
                }
            }
        } catch (SQLException | IOException e) {
            logger.severe("Error while checking book availability: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
        return false;
    }

    private static boolean searchMemberById(int memberId) {
        logger.info("Searching for member ID: " + memberId);
        try (Connection connection = SQLConnection.getConnection()) {
            String sql = "SELECT * FROM Members WHERE MEMBER_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, memberId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                logger.info("Member found: " + memberId);
                return true;
            } else {
                logger.warning("No member found with ID: " + memberId);
            }
        } catch (SQLException | IOException e) {
            logger.severe("Error while searching for member: " + e.getMessage());
            System.out.println("Error : " + e.getMessage());
        }
        return false;
    }
}
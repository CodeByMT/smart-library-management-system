package tests;

import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;

import models.Book;
import models.LibraryItem;
import models.Transaction;
import models.User;
import services.FileManager;
import services.LibraryService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibrarySystemTestSuite {

    public static void main(String[] args) {

        System.out.println("===== SMART LIBRARY SYSTEM TESTS =====");

        testAddItemAndSearch();
        testBorrowLimit();
        testOverdueFine();
        testReserveAndUndo();
        testIssueUndoStateConsistency();
        testReturnUndoStateConsistency();
        testCsvBookTitleWithComma();
        testCsvAuthorWithComma();
        testCsvUserNameWithComma();
        testValidEmail();
        testEmptyEmail();
        testInvalidEmail();
        testCsvUserEmailLoading();
        testCsvFieldWithQuotes();
        testMalformedCsvRow();
        testIncompleteCsvRow();
        testBooksRoundTrip();
        testUsersRoundTrip();
        testTransactionsRoundTrip();
        testSorting();

        System.out.println("===== ALL TESTS FINISHED =====");
    }

    public static void testAddItemAndSearch() {

        System.out.println("\nTest: Add Item and Search");

        LibraryService service = new LibraryService("test_data");

        service.addItemWithDetails(
                "T100",
                "Test Driven Development",
                "Kent Beck",
                "Book"
        );

        boolean found = false;

        for (LibraryItem item : service.getItems()) {

            if (item.getItemID().equals("T100")) {

                found = true;

                if (item.getTitle().equals("Test Driven Development")) {
                    System.out.println("PASS");
                } else {
                    System.out.println("FAIL");
                }
            }
        }

        if (!found) {
            System.out.println("FAIL - Item not found");
        }
    }

    public static void testBorrowLimit() {

        System.out.println("\nTest: Borrow Limit");

        try {

            LibraryService service =
                    new LibraryService("test_data");

            service.addUser("U100", "Talha");

            service.addItemWithDetails("B003", "Book 3", "Author", "Book");
            service.addItemWithDetails("B004", "Book 4", "Author", "Book");

            service.issueItem("U100", "B001", 1);
            service.issueItem("U100", "B002", 2);
            service.issueItem("U100", "B003", 3);

            service.issueItem("U100", "B004", 4);

            System.out.println("FAIL");

        } catch (ItemNotAvailableException e) {

            System.out.println("PASS");

        } catch (Exception e) {

            System.out.println("FAIL");
        }
    }

    public static void testOverdueFine() {

        System.out.println("\nTest: Overdue Fine");

        try {

            LibraryService service =
                    new LibraryService("test_data");

            service.issueItem("U001", "B001", 1);

            service.returnItem("U001", "B001", 11);

            System.out.println("FAIL");

        } catch (OverdueException e) {

            System.out.println("PASS");

        } catch (Exception e) {

            System.out.println("FAIL");
        }
    }

    public static void testReserveAndUndo() {

        System.out.println("\nTest: Reserve and Undo");

        try {

            LibraryService service =
                    new LibraryService("test_data");

            service.addItemWithDetails(
                    "B005",
                    "Modern Java",
                    "James Gosling",
                    "Book"
            );

            service.reserveItem("U002", "B005");

            LibraryItem item = null;

            for (LibraryItem i : service.getItems()) {

                if (i.getItemID().equals("B005")) {
                    item = i;
                }
            }

            if (item != null && item.peekReservation() != null) {

                boolean undone = service.undoLastTransaction();

                if (undone && item.peekReservation() == null) {
                    System.out.println("PASS");
                } else {
                    System.out.println("FAIL");
                }

            } else {

                System.out.println("FAIL");
            }

        } catch (Exception e) {

            System.out.println("FAIL");
        }
    }

    public static void testIssueUndoStateConsistency() {

        System.out.println("\nTest: Issue Undo State Consistency");

        try {
            LibraryService service = new LibraryService("test_data");

            service.addUser("U250", "State User A");
            service.addUser("U251", "State User B");
            service.addItemWithDetails("B250", "Queue Book", "Author", "Book");

            service.reserveItem("U250", "B250");
            service.reserveItem("U251", "B250");

            service.issueItem("U250", "B250", 1);

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B250")) {
                    item = libraryItem;
                }
            }

            if (item == null || item.isAvailable() || item.getBorrowCount() != 1) {
                System.out.println("FAIL");
                return;
            }

            boolean undone = service.undoLastTransaction();

            if (undone && item.isAvailable() && item.getBorrowCount() == 0
                    && item.peekReservation() != null && item.peekReservation().equals("U250")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }

        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testReturnUndoStateConsistency() {

        System.out.println("\nTest: Return Undo State Consistency");

        try {
            LibraryService service = new LibraryService("test_data");

            service.addUser("U260", "Return User");
            service.addItemWithDetails("B260", "Return Book", "Author", "Book");

            service.issueItem("U260", "B260", 1);
            service.returnItem("U260", "B260", 10);

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B260")) {
                    item = libraryItem;
                }
            }

            if (item == null || !item.isAvailable() || item.getBorrowCount() != 1) {
                System.out.println("FAIL");
                return;
            }

            boolean undone = service.undoLastTransaction();

            if (undone && !item.isAvailable() && item.getBorrowCount() == 1) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }

        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testCsvBookTitleWithComma() {
        System.out.println("\nTest: Book title with comma CSV round trip");
        LibraryItem item = new Book("B200", "Java, Advanced", "Author", true);
        String csv = item.toCsv();
        String[] parsed = parseCsvRow(csv);
        if (parsed.length == 5 && parsed[1].equals("Java, Advanced")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testCsvAuthorWithComma() {
        System.out.println("\nTest: Author with comma CSV round trip");
        LibraryItem item = new Book("B201", "Example Book", "Smith, Jane", true);
        String csv = item.toCsv();
        String[] parsed = parseCsvRow(csv);
        if (parsed.length == 5 && parsed[2].equals("Smith, Jane")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testCsvUserNameWithComma() {
        System.out.println("\nTest: User name with comma CSV round trip");
        User user = new User("U200", "Doe, John", "john@example.com", 3);
        String csv = user.toCsv();
        String[] parsed = parseCsvRow(csv);
        if (parsed.length == 4 && parsed[1].equals("Doe, John")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testValidEmail() {
        System.out.println("\nTest: Valid email is accepted");
        User user = new User("U400", "Valid User", "valid@example.com", 3);
        if ("valid@example.com".equals(user.getEmail())) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testEmptyEmail() {
        System.out.println("\nTest: Empty email is allowed");
        User user = new User("U401", "Empty Email User", "", 3);
        if (user.getEmail().isEmpty()) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testInvalidEmail() {
        System.out.println("\nTest: Invalid email is rejected");
        try {
            new User("U402", "Invalid Email User", "not-an-email", 3);
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }

    public static void testCsvUserEmailLoading() {
        System.out.println("\nTest: CSV input email handling");
        File file = new File("test_data/csv_users_email_validation.csv");
        file.getParentFile().mkdirs();

        try {
            java.nio.file.Files.write(file.toPath(), (
                    "UserID,Name,Email,MaxBorrowLimit\n" +
                    "U500,Valid User,valid@example.com,3\n" +
                    "U501,Empty Email User,,3\n" +
                    "U502,Bad Email User,not-an-email,3\n" +
                    "U503,Another Valid User,second@example.com,3\n").getBytes());

            ArrayList<User> loaded = FileManager.loadUsers(file.getAbsolutePath());
            if (loaded.size() == 3
                    && loaded.get(0).getEmail().equals("valid@example.com")
                    && loaded.get(1).getEmail().isEmpty()
                    && loaded.get(2).getEmail().equals("second@example.com")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testCsvFieldWithQuotes() {
        System.out.println("\nTest: CSV field with quotes");
        LibraryItem item = new Book("B202", "He said \"Hello\"", "Author \"Quoted\"", true);
        String csv = item.toCsv();
        String[] parsed = parseCsvRow(csv);
        if (parsed.length == 5 && parsed[1].equals("He said \"Hello\"") && parsed[2].equals("Author \"Quoted\"")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testMalformedCsvRow() {
        System.out.println("\nTest: Malformed CSV row is skipped safely");
        File file = new File("test_data/malformed_books.csv");
        file.getParentFile().mkdirs();

        try {
            java.nio.file.Files.write(file.toPath(), (
                    "ItemID,Title,Author,Type,Availability\n" +
                    "B500,\"Broken title,Book,true\n").getBytes());
            ArrayList<LibraryItem> loaded = FileManager.loadItems(file.getAbsolutePath());
            if (loaded.isEmpty()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testIncompleteCsvRow() {
        System.out.println("\nTest: Incomplete CSV row is skipped safely");
        File file = new File("test_data/incomplete_books.csv");
        file.getParentFile().mkdirs();

        try {
            java.nio.file.Files.write(file.toPath(), (
                    "ItemID,Title,Author,Type,Availability\n" +
                    "B600,Example Title,Example Author,Book\n").getBytes());
            ArrayList<LibraryItem> loaded = FileManager.loadItems(file.getAbsolutePath());
            if (loaded.isEmpty()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testBooksRoundTrip() {
        System.out.println("\nTest: Books save/load round trip");
        ArrayList<LibraryItem> items = new ArrayList<>();
        items.add(new Book("B300", "Book, Title", "Author, Name", true));
        items.add(new Book("B301", "Second\"Book\"", "Jane \"Writer\"", false));

        File file = new File("test_data/csv_books_roundtrip.csv");
        file.getParentFile().mkdirs();
        FileManager.saveItems(file.getAbsolutePath(), items);
        ArrayList<LibraryItem> loaded = FileManager.loadItems(file.getAbsolutePath());

        if (loaded.size() == 2 && loaded.get(0).getTitle().equals("Book, Title")
                && loaded.get(1).getAuthor().equals("Jane \"Writer\"")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testUsersRoundTrip() {
        System.out.println("\nTest: Users save/load round trip");
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("U300", "Doe, Jane", "jane@example.com", 3));
        users.add(new User("U301", "Smith \"Test\"", "smith@example.com", 2));

        File file = new File("test_data/csv_users_roundtrip.csv");
        file.getParentFile().mkdirs();
        FileManager.saveUsers(file.getAbsolutePath(), users);
        ArrayList<User> loaded = FileManager.loadUsers(file.getAbsolutePath());

        if (loaded.size() == 2 && loaded.get(0).getName().equals("Doe, Jane")
                && loaded.get(1).getName().equals("Smith \"Test\"")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testTransactionsRoundTrip() {
        System.out.println("\nTest: Transactions save/load round trip");
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("T300", "U300", "B300", 1, 8, 0, 0.0));
        transactions.add(new Transaction("T301", "U301", "B301", 2, 9, 10, 50.0));

        File file = new File("test_data/csv_transactions_roundtrip.csv");
        file.getParentFile().mkdirs();
        FileManager.saveTransactions(file.getAbsolutePath(), transactions);
        ArrayList<Transaction> loaded = FileManager.loadTransactions(file.getAbsolutePath());

        if (loaded.size() == 2 && loaded.get(1).getFine() == 50.0
                && loaded.get(0).getUserID().equals("U300")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    private static String[] parseCsvRow(String csv) {
        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csv.length(); i++) {
            char ch = csv.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    public static void testSorting() {

        System.out.println("\nTest: Sorting");

        LibraryService service = new LibraryService("test_data");

        service.sortItems();

        List<LibraryItem> items = service.getItems();

        boolean sorted = true;

        for (int i = 1; i < items.size(); i++) {

            String first = items.get(i - 1).getTitle();
            String second = items.get(i).getTitle();

            if (first.compareToIgnoreCase(second) > 0) {
                sorted = false;
            }
        }

        if (sorted) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }
}

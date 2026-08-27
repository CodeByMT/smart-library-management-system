package tests;

import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;

import models.Book;
import models.EBook;
import models.Journal;
import models.LibraryItem;
import models.Transaction;
import models.User;
import services.FileManager;
import services.LibraryService;
import utils.SortUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class LibrarySystemTestSuite {

    public static void main(String[] args) {

        System.out.println("===== SMART LIBRARY SYSTEM TESTS =====");

        testAddItemAndSearch();
        testCollectionAccessorsAreReadOnly();
        testLocalDateTransactionLifecycle();
        testLocalDateValidation();
        testIsoTransactionCsvRoundTrip();
        testBorrowingHistoryAndStateRebuild();
        testFinePaymentTracking();
        testReservationCancellation();
        testIntegrityCheck();
        testSearchAndFilterCapabilities();
        testComparatorSortingOptions();
        testAddValidUser();
        testDuplicateUserIdRejected();
        testBlankUserIdRejected();
        testOptionalValidEmail();
        testServiceEmptyEmailAllowed();
        testServiceInvalidEmailRejected();
        testListUsers();
        testSearchUserById();
        testUserPersistence();
        testHistoricalBorrowCountIncreasesOnIssue();
        testHistoricalBorrowCountUnchangedOnNormalReturn();
        testHistoricalBorrowCountUnchangedOnUndoReturn();
        testHistoricalBorrowCountDecreasesOnUndoIssue();
        try {
            testHistoricalBorrowCountRebuiltFromTransactions();
        } catch (Exception e) {
            System.out.println("FAIL");
        }
        testMostBorrowedReportUsesHistoricalCount();
        testReservationCanBeAdded();
        testReservationQueueIsFifo();
        testDuplicateReservationRejected();
        testIssueRespectsHeadReservation();
        testHeadReservationRemovedOnSuccessfulIssue();
        testUndoIssueRestoresReservationAtFront();
        testFreshLibraryServiceDoesNotReconstructReservationStateFromCsv();
        testBooksCsvHeaderRemainsUnchanged();
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

    public static void testCollectionAccessorsAreReadOnly() {
        System.out.println("\nTest: Collection accessors are read-only");
        LibraryService service = new LibraryService("test_data");
        boolean protectedItems = false;
        boolean protectedUsers = false;
        boolean protectedBorrowedItems = false;

        try {
            service.getItems().clear();
        } catch (UnsupportedOperationException e) {
            protectedItems = true;
        }

        try {
            service.getUsers().clear();
        } catch (UnsupportedOperationException e) {
            protectedUsers = true;
        }

        try {
            User user = service.getUsers().get(0);
            LibraryItem item = service.getItems().get(0);
            user.borrowItem(item);
            user.getBorrowedItems().clear();
        } catch (UnsupportedOperationException e) {
            protectedBorrowedItems = true;
        }

        if (protectedItems && protectedUsers && protectedBorrowedItems) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testLocalDateTransactionLifecycle() {
        System.out.println("\nTest: LocalDate transaction lifecycle and overdue report");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("DATE_USER", "Date User");
            service.addItemWithDetails("DATE_BOOK", "Date Book", "Author", "Book");
            LocalDate issueDate = LocalDate.of(2026, 8, 1);
            service.issueItem("DATE_USER", "DATE_BOOK", issueDate);

            Transaction transaction = service.getTransactions().get(service.getTransactions().size() - 1);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(output));
            try {
                service.showReports(issueDate.plusDays(10));
            } finally {
                System.setOut(originalOut);
            }

            boolean reportShowsOverdue = output.toString().contains("Overdue items: 1");
            boolean datesAreCorrect = transaction.getIssueDate().equals(issueDate)
                    && transaction.getDueDate().equals(issueDate.plusDays(7));

            try {
                service.returnItem("DATE_USER", "DATE_BOOK", issueDate.plusDays(11));
                System.out.println("FAIL");
            } catch (OverdueException e) {
                boolean fineIsCorrect = transaction.getReturnDate().equals(issueDate.plusDays(11))
                        && transaction.getFine() == 100.0;
                System.out.println(reportShowsOverdue && datesAreCorrect && fineIsCorrect ? "PASS" : "FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testIsoTransactionCsvRoundTrip() {
        System.out.println("\nTest: ISO transaction CSV round trip");
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("ISO001", "U001", "B001",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 8),
                LocalDate.of(2026, 8, 12), 100.0));

        File file = new File("test_data/csv_transactions_iso.csv");
        file.getParentFile().mkdirs();
        FileManager.saveTransactions(file.getAbsolutePath(), transactions);
        ArrayList<Transaction> loaded = FileManager.loadTransactions(file.getAbsolutePath());

        if (loaded.size() == 1
                && loaded.get(0).getIssueDate().equals(LocalDate.of(2026, 8, 1))
                && loaded.get(0).getDueDate().equals(LocalDate.of(2026, 8, 8))
                && loaded.get(0).getReturnDate().equals(LocalDate.of(2026, 8, 12))) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testBorrowingHistoryAndStateRebuild() {
        System.out.println("\nTest: Borrowing history and active state rebuild");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("HISTORY_USER", "History User");
            service.addItemWithDetails("HISTORY_BOOK", "History Book", "Author", "Book");
            service.issueItem("HISTORY_USER", "HISTORY_BOOK", LocalDate.of(2026, 8, 1));

            List<Transaction> userHistory = service.getBorrowingHistoryForUser("history_user");
            List<Transaction> itemHistory = service.getBorrowingHistoryForItem("history_book");
            User user = service.getUsers().stream()
                    .filter(candidate -> candidate.getId().equals("HISTORY_USER"))
                    .findFirst().get();

            boolean readOnly;
            try {
                userHistory.clear();
                readOnly = false;
            } catch (UnsupportedOperationException e) {
                readOnly = true;
            }

            System.out.println(userHistory.size() == 1 && itemHistory.size() == 1
                    && user.getBorrowedItems().size() == 1 && readOnly ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testFinePaymentTracking() {
        System.out.println("\nTest: Fine payment tracking and persistence");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("PAYMENT_USER", "Payment User");
            service.addItemWithDetails("PAYMENT_BOOK", "Payment Book", "Author", "Book");
            LocalDate issueDate = LocalDate.of(2026, 8, 1);
            service.issueItem("PAYMENT_USER", "PAYMENT_BOOK", issueDate);
            try {
                service.returnItem("PAYMENT_USER", "PAYMENT_BOOK", issueDate.plusDays(11));
            } catch (OverdueException ignored) {
            }
            Transaction transaction = service.getBorrowingHistoryForUser("PAYMENT_USER").get(0);
            service.recordFinePayment(transaction.getTransactionID(), 50.0, issueDate.plusDays(12));
            boolean partialPayment = transaction.getPaidFine() == 50.0
                    && transaction.getOutstandingFine() == 50.0;

            File file = new File("test_data/csv_transactions_payment.csv");
            FileManager.saveTransactions(file.getAbsolutePath(), new ArrayList<>(service.getTransactions()));
            ArrayList<Transaction> loaded = FileManager.loadTransactions(file.getAbsolutePath());
            boolean persisted = loaded.size() == service.getTransactions().size()
                    && loaded.stream().anyMatch(saved -> saved.getTransactionID().equals(transaction.getTransactionID())
                    && saved.getPaidFine() == 50.0
                    && saved.getPaymentDate().equals(issueDate.plusDays(12)));
            System.out.println(partialPayment && persisted ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testReservationCancellation() {
        System.out.println("\nTest: Reservation cancellation preserves FIFO order");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("QUEUE_USER_ONE", "Queue User One");
            service.addUser("QUEUE_USER_TWO", "Queue User Two");
            service.addItemWithDetails("QUEUE_BOOK", "Queue Book", "Author", "Book");
            service.reserveItem("QUEUE_USER_ONE", "QUEUE_BOOK");
            service.reserveItem("QUEUE_USER_TWO", "QUEUE_BOOK");
            boolean cancelled = service.cancelReservation("queue_user_one", "queue_book");
            List<String> queue = service.getReservationQueue("QUEUE_BOOK");
            System.out.println(cancelled && queue.size() == 1
                    && queue.get(0).equals("QUEUE_USER_TWO") ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testIntegrityCheck() {
        System.out.println("\nTest: Data integrity check");
        try {
            LibraryService service = new LibraryService("data");
            List<String> issues = service.validateDataIntegrity();
            System.out.println(issues.stream().anyMatch(issue -> issue.contains("J302")) ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testLocalDateValidation() {
        System.out.println("\nTest: LocalDate return validation");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("DATE_VALIDATION_USER", "Date Validation User");
            service.addItemWithDetails("DATE_VALIDATION_BOOK", "Date Validation Book", "Author", "Book");
            LocalDate issueDate = LocalDate.of(2026, 8, 20);
            service.issueItem("DATE_VALIDATION_USER", "DATE_VALIDATION_BOOK", issueDate);
            service.returnItem("DATE_VALIDATION_USER", "DATE_VALIDATION_BOOK", issueDate.minusDays(1));
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testSearchAndFilterCapabilities() {
        System.out.println("\nTest: Search and filter capabilities");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("SEARCH_USER", "Search User");
            service.addItemWithDetails("SEARCH_BOOK", "Algorithms in Practice", "Grace Hopper", "Book");
            service.addItemWithDetails("SEARCH_EBOOK", "Java Patterns", "Grace Hopper", "EBook");
            service.addItemWithDetails("SEARCH_JOURNAL", "Distributed Systems", "Alan Turing", "Journal");
            service.issueItem("SEARCH_USER", "SEARCH_EBOOK", LocalDate.of(2026, 8, 1));

            List<LibraryItem> authorMatches = service.searchItems("grace hopper");
            List<LibraryItem> idMatches = service.searchItems("search_book");
            List<LibraryItem> titleMatches = service.searchItems("distributed");
            List<LibraryItem> availableBooks = service.filterItems("BOOK", true);
            List<LibraryItem> unavailableDigital = service.filterItems(
                    new HashSet<>(Arrays.asList("ebook", "journal")), false);

                boolean matchesWork = authorMatches.size() == 2
                    && idMatches.size() == 1
                    && titleMatches.size() == 1
                    && availableBooks.stream().anyMatch(item -> item.getItemID().equals("SEARCH_BOOK"))
                    && unavailableDigital.size() == 1
                    && unavailableDigital.get(0).getItemID().equals("SEARCH_EBOOK");

            boolean resultIsReadOnly;
            try {
                availableBooks.clear();
                resultIsReadOnly = false;
            } catch (UnsupportedOperationException e) {
                resultIsReadOnly = true;
            }

            System.out.println(matchesWork && resultIsReadOnly ? "PASS" : "FAIL");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testComparatorSortingOptions() {
        System.out.println("\nTest: Comparator sorting options");
        List<LibraryItem> items = new ArrayList<>();
        LibraryItem zulu = new Book("SORT_Z", "Zulu", "Zed Author", true);
        LibraryItem alpha = new Journal("SORT_A", "Alpha", "Ava Author", false);
        LibraryItem middle = new EBook("SORT_M", "Middle", "Mia Author", true);
        zulu.issueItem(null);
        zulu.issueItem(null);
        middle.issueItem(null);
        middle.setAvailable(true);
        items.add(zulu);
        items.add(alpha);
        items.add(middle);

        SortUtil.sort(items, SortUtil.SortOption.AUTHOR);
        boolean authorSorted = items.get(0) == alpha;
        SortUtil.sort(items, SortUtil.SortOption.AVAILABILITY);
        boolean availabilitySorted = items.get(0) == middle;
        SortUtil.sort(items, SortUtil.SortOption.BORROW_COUNT);
        boolean borrowCountSorted = items.get(0) == zulu;

        System.out.println(authorSorted && availabilitySorted && borrowCountSorted ? "PASS" : "FAIL");
    }

    public static void testAddValidUser() {
        System.out.println("\nTest: Add valid user");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("U900", "Valid User", "valid@example.com");
            System.out.println("PASS");
        } catch (IllegalArgumentException e) {
            System.out.println("FAIL");
        }
    }

    public static void testDuplicateUserIdRejected() {
        System.out.println("\nTest: Duplicate user ID rejected");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("U001", "Duplicate User", "dup@example.com");
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }

    public static void testBlankUserIdRejected() {
        System.out.println("\nTest: Blank user ID rejected");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("   ", "Blank User", "blank@example.com");
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }

    public static void testOptionalValidEmail() {
        System.out.println("\nTest: Optional valid email accepted");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("U901", "Email User", "email@example.com");
            System.out.println("PASS");
        } catch (IllegalArgumentException e) {
            System.out.println("FAIL");
        }
    }

    public static void testServiceEmptyEmailAllowed() {
        System.out.println("\nTest: Empty email allowed");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("U902", "Empty Email User", "");
            System.out.println("PASS");
        } catch (IllegalArgumentException e) {
            System.out.println("FAIL");
        }
    }

    public static void testServiceInvalidEmailRejected() {
        System.out.println("\nTest: Invalid email rejected");
        LibraryService service = new LibraryService("test_data");
        try {
            service.addUser("U903", "Bad Email User", "not-an-email");
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS");
        }
    }

    public static void testListUsers() {
        System.out.println("\nTest: List users");
        LibraryService service = new LibraryService("test_data");
        boolean hasUsers = !service.getUsers().isEmpty();
        if (hasUsers) {
            service.viewUsers();
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testSearchUserById() {
        System.out.println("\nTest: Search user by ID");
        LibraryService service = new LibraryService("test_data");
        User user = service.getUsers().isEmpty() ? null : service.getUsers().get(0);
        if (user != null) {
            service.searchUserById(user.getId());
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testUserPersistence() {
        System.out.println("\nTest: User persistence to CSV");
        LibraryService service = new LibraryService("test_data");
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("U920", "CSV User", "csv@example.com", 3));
        users.add(new User("U921", "CSV Empty Email User", "", 2));

        File file = new File("test_data/csv_users_phase3.csv");
        file.getParentFile().mkdirs();
        FileManager.saveUsers(file.getAbsolutePath(), users);
        ArrayList<User> loaded = FileManager.loadUsers(file.getAbsolutePath());

        if (loaded.size() == 2
                && loaded.get(0).getEmail().equals("csv@example.com")
                && loaded.get(1).getEmail().isEmpty()) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testHistoricalBorrowCountIncreasesOnIssue() {
        System.out.println("\nTest: Historical borrowCount increases on issue");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U930", "Historical Borrow User");
            service.addItemWithDetails("B930", "Historical Count Book", "Author", "Book");

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B930")) {
                    item = libraryItem;
                }
            }

            if (item == null) {
                System.out.println("FAIL");
                return;
            }

            int before = item.getBorrowCount();
            service.issueItem("U930", "B930", 1);
            if (item.getBorrowCount() == before + 1) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testHistoricalBorrowCountUnchangedOnNormalReturn() {
        System.out.println("\nTest: Historical borrowCount unchanged on normal return");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U931", "Historical Return User");
            service.addItemWithDetails("B931", "Historical Return Book", "Author", "Book");
            service.issueItem("U931", "B931", 1);

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B931")) {
                    item = libraryItem;
                }
            }

            if (item == null) {
                System.out.println("FAIL");
                return;
            }

            int before = item.getBorrowCount();
            service.returnItem("U931", "B931", 10);
            if (item.getBorrowCount() == before && item.isAvailable()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testHistoricalBorrowCountUnchangedOnUndoReturn() {
        System.out.println("\nTest: Historical borrowCount unchanged on undo return");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U932", "Historical Undo Return User");
            service.addItemWithDetails("B932", "Historical Undo Return Book", "Author", "Book");
            service.issueItem("U932", "B932", 1);
            service.returnItem("U932", "B932", 10);

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B932")) {
                    item = libraryItem;
                }
            }

            if (item == null) {
                System.out.println("FAIL");
                return;
            }

            int before = item.getBorrowCount();
            service.undoLastTransaction();
            if (item.getBorrowCount() == before && !item.isAvailable()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testHistoricalBorrowCountDecreasesOnUndoIssue() {
        System.out.println("\nTest: Historical borrowCount decreases on undo issue");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U933", "Historical Undo Issue User");
            service.addItemWithDetails("B933", "Historical Undo Issue Book", "Author", "Book");
            service.issueItem("U933", "B933", 1);

            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B933")) {
                    item = libraryItem;
                }
            }

            if (item == null) {
                System.out.println("FAIL");
                return;
            }

            int before = item.getBorrowCount();
            service.undoLastTransaction();
            if (item.getBorrowCount() == before - 1 && item.isAvailable()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testHistoricalBorrowCountRebuiltFromTransactions() throws Exception {
        System.out.println("\nTest: Historical borrowCount rebuilt from transaction history");

        File dir = new File("test_data/borrow_count_rebuild");
        dir.mkdirs();

        File booksFile = new File(dir, "books.csv");
        File usersFile = new File(dir, "users.csv");
        File transactionsFile = new File(dir, "transactions.csv");

        java.nio.file.Files.writeString(booksFile.toPath(),
                "ItemID,Title,Author,Type,Availability\nB990,History Book,Author,Book,true\n");
        java.nio.file.Files.writeString(usersFile.toPath(),
                "UserID,Name,Email,MaxBorrowLimit\nU990,History User,history@example.com,3\n");
        java.nio.file.Files.writeString(transactionsFile.toPath(),
                "TransactionID,UserID,ItemID,IssueDay,DueDay,ReturnDay,Fine\n" +
                "T001,U990,B990,1,8,5,0.0\n" +
                "T002,U990,B990,9,16,0,0.0\n");

        LibraryService service = new LibraryService(dir.getAbsolutePath());
        LibraryItem item = service.getItems().get(0);
        if (item.getBorrowCount() == 2 && !item.isAvailable()) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void testMostBorrowedReportUsesHistoricalCount() {
        System.out.println("\nTest: Most-borrowed report uses historical borrow count");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U934", "Report User A");
            service.addUser("U935", "Report User B");
            service.addItemWithDetails("B934", "Report Book One", "Author", "Book");
            service.addItemWithDetails("B935", "Report Book Two", "Author", "Book");

            service.issueItem("U934", "B934", 1);
            service.returnItem("U934", "B934", 10);
            service.issueItem("U935", "B934", 11);
            service.returnItem("U935", "B934", 20);

            service.issueItem("U934", "B935", 3);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(output));
            try {
                service.showReports();
            } finally {
                System.setOut(originalOut);
            }

            String report = output.toString();
            if (report.contains("Most borrowed item: Report Book One (2 times)")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testReservationCanBeAdded() {
        System.out.println("\nTest: Reservation can be added");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U960", "Reservation User");
            service.addItemWithDetails("B960", "Reservation Book", "Author", "Book");
            service.reserveItem("U960", "B960");
            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B960")) {
                    item = libraryItem;
                }
            }
            if (item != null && "U960".equals(item.peekReservation())) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testReservationQueueIsFifo() {
        System.out.println("\nTest: Reservation queue is FIFO");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U961", "First User");
            service.addUser("U962", "Second User");
            service.addItemWithDetails("B961", "FIFO Book", "Author", "Book");
            service.reserveItem("U961", "B961");
            service.reserveItem("U962", "B961");
            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B961")) {
                    item = libraryItem;
                }
            }
            if (item != null && "U961".equals(item.peekReservation()) && item.getReservationList().contains("U961") && item.getReservationList().contains("U962")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testDuplicateReservationRejected() {
        System.out.println("\nTest: Duplicate reservation rejected");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U963", "Duplicate User");
            service.addItemWithDetails("B963", "Duplicate Book", "Author", "Book");
            service.reserveItem("U963", "B963");
            service.reserveItem("U963", "B963");
            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B963")) {
                    item = libraryItem;
                }
            }
            if (item != null && item.getReservationList().equals("U963")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testIssueRespectsHeadReservation() {
        System.out.println("\nTest: Issue respects queue head reservation");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U964", "Queue Head User");
            service.addUser("U965", "Other User");
            service.addItemWithDetails("B964", "Reserved Book", "Author", "Book");
            service.reserveItem("U964", "B964");
            service.issueItem("U965", "B964", 1);
            System.out.println("FAIL");
        } catch (ItemNotAvailableException e) {
            System.out.println("PASS");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testHeadReservationRemovedOnSuccessfulIssue() {
        System.out.println("\nTest: Head reservation removed on successful issue");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U966", "Head Reservation User");
            service.addItemWithDetails("B966", "Queue Removal Book", "Author", "Book");
            service.reserveItem("U966", "B966");
            service.issueItem("U966", "B966", 1);
            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B966")) {
                    item = libraryItem;
                }
            }
            if (item != null && item.peekReservation() == null && !item.isAvailable()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testUndoIssueRestoresReservationAtFront() {
        System.out.println("\nTest: Undo issue restores reservation at front");
        try {
            LibraryService service = new LibraryService("test_data");
            service.addUser("U967", "Front Restore User A");
            service.addUser("U968", "Front Restore User B");
            service.addItemWithDetails("B967", "Restore Queue Book", "Author", "Book");
            service.reserveItem("U967", "B967");
            service.reserveItem("U968", "B967");
            service.issueItem("U967", "B967", 1);
            service.undoLastTransaction();
            LibraryItem item = null;
            for (LibraryItem libraryItem : service.getItems()) {
                if (libraryItem.getItemID().equals("B967")) {
                    item = libraryItem;
                }
            }
            if (item != null && "U967".equals(item.peekReservation()) && item.isAvailable()) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }

    public static void testFreshLibraryServiceDoesNotReconstructReservationStateFromCsv() {
        System.out.println("\nTest: Fresh LibraryService does not reconstruct reservation state from CSV");
        File dir = null;
        try {
            dir = java.nio.file.Files.createTempDirectory("lib_session_restart_").toFile();

            File booksFile = new File(dir, "books.csv");
            File usersFile = new File(dir, "users.csv");
            File transactionsFile = new File(dir, "transactions.csv");

            java.nio.file.Files.writeString(booksFile.toPath(),
                    "ItemID,Title,Author,Type,Availability\n");
            java.nio.file.Files.writeString(usersFile.toPath(),
                    "UserID,Name,Email,MaxBorrowLimit\n");
            java.nio.file.Files.writeString(transactionsFile.toPath(),
                    "TransactionID,UserID,ItemID,IssueDay,DueDay,ReturnDay,Fine\n");

            String uniqueUserId = "RES_USER_" + System.nanoTime();
            String uniqueItemId = "RES_ITEM_" + System.nanoTime();

            LibraryService first = new LibraryService(dir.getAbsolutePath());
            first.addUser(uniqueUserId, "Session User");
            first.addItemWithDetails(uniqueItemId, "Session Book", "Author", "Book");

            LibraryItem item = null;
            for (LibraryItem libraryItem : first.getItems()) {
                if (libraryItem.getItemID().equals(uniqueItemId)) {
                    item = libraryItem;
                }
            }

            if (item == null) {
                System.out.println("FAIL");
                return;
            }

            first.reserveItem(uniqueUserId, uniqueItemId);
            if (item.peekReservation() == null || !uniqueUserId.equals(item.peekReservation())) {
                System.out.println("FAIL");
                return;
            }

            first.saveData();

            LibraryService fresh = new LibraryService(dir.getAbsolutePath());
            if (fresh.getItems().size() != 1 || fresh.getUsers().size() != 1) {
                System.out.println("FAIL");
                return;
            }

            LibraryItem freshItem = fresh.getItems().get(0);
            if (freshItem.peekReservation() == null && "None".equals(freshItem.getReservationList())
                    && fresh.getUsers().get(0).getId().equals(uniqueUserId)) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
        } finally {
            if (dir != null && dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteRecursively(file);
                        } else {
                            file.delete();
                        }
                    }
                }
                dir.delete();
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    public static void testBooksCsvHeaderRemainsUnchanged() {
        System.out.println("\nTest: books.csv schema remains unchanged");
        try {
            File file = new File("data/books.csv");
            String header = java.nio.file.Files.readAllLines(file.toPath()).get(0);
            String[] columns = header.split(",");
            if (columns.length == 5 && columns[0].equals("ItemID") && columns[4].equals("Availability") && !header.contains("Reservation")) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL");
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

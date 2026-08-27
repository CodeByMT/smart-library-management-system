package main;

import exceptions.InvalidUserException;
import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;
import models.LibraryItem;
import models.Transaction;
import services.LibraryService;
import utils.SortUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line launcher for the Smart Library Management System.
 *
 * <p>
 * This class provides a simple menu-driven interface for users to manage
 * library items, library members, and library transactions.</p>
 */
public class Main {
    private static final int MIN_MENU_OPTION = 1;
    private static final int MAX_MENU_OPTION = 6;

    public static void main(String[] args) {
        String dataDirectory = getDataDirectory();
        LibraryService libraryService = new LibraryService(dataDirectory);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running && scanner.hasNextLine()) {
            printMenu();
            int choice = readValidMenuChoice(scanner);

            switch (choice) {
                case 1:
                    catalogMenu(scanner, libraryService);
                    break;
                case 2:
                    userMenu(scanner, libraryService);
                    break;
                case 3:
                    borrowingMenu(scanner, libraryService);
                    break;
                case 4:
                    reportsMenu(scanner, libraryService);
                    break;
                case 5:
                    libraryService.saveData();
                    break;
                case 6:
                    libraryService.saveData();
                    running = false;
                    System.out.println("Session saved. Goodbye!");
                    break;
                default:
                    System.out.println("Please choose an option from the menu.");
                    break;
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("====================================================");
        System.out.println("              SMART LIBRARY MANAGEMENT              ");
        System.out.println("====================================================");
        System.out.println("CATALOG");
        System.out.println("  1. Catalog and item tools");
        System.out.println("  2. Users and borrowing history");
        System.out.println("TRANSACTIONS");
        System.out.println("  3. Borrow, return, reserve, and payments");
        System.out.println("INSIGHTS");
        System.out.println("  4. Reports and data integrity");
        System.out.println("  5. Save data");
        System.out.println("  6. Save and exit");
        System.out.println("====================================================");
    }

    private static int readInteger(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static int readValidMenuChoice(Scanner scanner) {
        while (true) {
            int choice = readInteger(scanner, "Enter your choice: ");
            if (choice >= MIN_MENU_OPTION && choice <= MAX_MENU_OPTION) {
                return choice;
            }
            System.out.println("Please choose a valid menu option from "
                    + MIN_MENU_OPTION + " to " + MAX_MENU_OPTION + ".");
        }
    }

    private static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private static String readOptionalString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String getDataDirectory() {
        String dataDirectory = System.getenv("LIBRARY_DATA_DIR");
        return (dataDirectory == null || dataDirectory.trim().isEmpty()) ? "data" : dataDirectory.trim();
    }

    private static void catalogMenu(Scanner scanner, LibraryService service) {
        while (scanner.hasNextLine()) {
            printSection("CATALOG AND ITEM TOOLS");
            System.out.println("1. Add item");
            System.out.println("2. View all items");
            System.out.println("3. Search by ID, title, or author");
            System.out.println("4. Filter by type and availability");
            System.out.println("5. Sort items");
            System.out.println("0. Back");
            int choice = readInteger(scanner, "Select an action: ");
            switch (choice) {
                case 1:
                    addItem(scanner, service);
                    break;
                case 2:
                    printItems(service.getItems());
                    break;
                case 3:
                    String keyword = readNonEmptyString(scanner, "Search keyword: ");
                    printItems(service.searchItems(keyword));
                    break;
                case 4:
                    filterItems(scanner, service);
                    break;
                case 5:
                    sortItems(scanner, service);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Please choose an option from 0 to 5.");
            }
        }
    }

    private static void userMenu(Scanner scanner, LibraryService service) {
        while (scanner.hasNextLine()) {
            printSection("USERS AND HISTORY");
            System.out.println("1. Add user");
            System.out.println("2. View users");
            System.out.println("3. Find user");
            System.out.println("4. View borrowing history");
            System.out.println("0. Back");
            int choice = readInteger(scanner, "Select an action: ");
            switch (choice) {
                case 1:
                    addUser(scanner, service);
                    break;
                case 2:
                    printUsers(service);
                    break;
                case 3:
                    searchUser(scanner, service);
                    break;
                case 4:
                    viewBorrowingHistory(scanner, service);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Please choose an option from 0 to 4.");
            }
        }
    }

    private static void borrowingMenu(Scanner scanner, LibraryService service) {
        while (scanner.hasNextLine()) {
            printSection("TRANSACTION WORKFLOWS");
            System.out.println("1. Issue item");
            System.out.println("2. Return item");
            System.out.println("3. Reserve item");
            System.out.println("4. View reservation queue");
            System.out.println("5. Cancel reservation");
            System.out.println("6. Record fine payment");
            System.out.println("7. Undo last action");
            System.out.println("0. Back");
            int choice = readInteger(scanner, "Select an action: ");
            switch (choice) {
                case 1:
                    issueItem(scanner, service);
                    break;
                case 2:
                    returnItem(scanner, service);
                    break;
                case 3:
                    reserveItem(scanner, service);
                    break;
                case 4:
                    viewReservationQueue(scanner, service);
                    break;
                case 5:
                    cancelReservation(scanner, service);
                    break;
                case 6:
                    recordFinePayment(scanner, service);
                    break;
                case 7:
                    service.undoLastTransaction();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Please choose an option from 0 to 7.");
            }
        }
    }

    private static void reportsMenu(Scanner scanner, LibraryService service) {
        while (scanner.hasNextLine()) {
            printSection("REPORTS AND MAINTENANCE");
            System.out.println("1. Show library report");
            System.out.println("2. Check data integrity");
            System.out.println("3. Help");
            System.out.println("0. Back");
            int choice = readInteger(scanner, "Select an action: ");
            switch (choice) {
                case 1:
                    service.showReports();
                    break;
                case 2:
                    checkDataIntegrity(service);
                    break;
                case 3:
                    printHelp();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Please choose an option from 0 to 3.");
            }
        }
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("---------------- " + title + " ----------------");
    }

    private static void printItems(List<LibraryItem> items) {
        if (items.isEmpty()) {
            System.out.println("No matching items found.");
            return;
        }
        System.out.println();
        System.out.printf("%-14s %-30s %-12s %-24s %-10s%n", "ID", "TITLE", "TYPE", "AUTHOR", "STATUS");
        System.out.println("------------------------------------------------------------------------------------");
        for (LibraryItem item : items) {
            System.out.printf("%-14s %-30s %-12s %-24s %-10s%n",
                    clip(item.getItemID(), 14), clip(item.getTitle(), 30), item.getType(),
                    clip(item.getAuthor().isEmpty() ? "Unknown" : item.getAuthor(), 24),
                    item.isAvailable() ? "Available" : "Issued");
        }
    }

    private static void printUsers(LibraryService service) {
        if (service.getUsers().isEmpty()) {
            System.out.println("No users are registered yet.");
            return;
        }
        System.out.println();
        System.out.printf("%-16s %-24s %-30s %s%n", "ID", "NAME", "EMAIL", "BORROWED");
        System.out.println("--------------------------------------------------------------------------");
        for (models.User user : service.getUsers()) {
            System.out.printf("%-16s %-24s %-30s %d%n", clip(user.getId(), 16),
                    clip(user.getName(), 24), clip(user.getEmail().isEmpty() ? "Not provided" : user.getEmail(), 30),
                    user.getBorrowedItems().size());
        }
    }

    private static void filterItems(Scanner scanner, LibraryService service) {
        String typeInput = readOptionalString(scanner, "Types (Book,EBook,Journal) or blank for all: ");
        Boolean available = readAvailability(scanner);
        java.util.Set<String> types = new HashSet<>(Arrays.asList(typeInput.split(",")));
        printItems(service.filterItems(types, available));
    }

    private static Boolean readAvailability(Scanner scanner) {
        while (true) {
            System.out.println("Availability: 1) All  2) Available  3) Issued");
            int choice = readInteger(scanner, "Choose availability: ");
            if (choice == 1) {
                return null;
            }
            if (choice == 2) {
                return true;
            }
            if (choice == 3) {
                return false;
            }
            System.out.println("Please choose 1, 2, or 3.");
        }
    }

    private static void sortItems(Scanner scanner, LibraryService service) {
        System.out.println("Sort by: 1) Title  2) Author  3) Type  4) Availability  5) Borrow count");
        int choice = readInteger(scanner, "Choose sort field: ");
        if (choice == 1) {
            service.sortItems();
        } else if (choice >= 2 && choice <= 5) {
            service.sortItemsBy(SortUtil.SortOption.values()[choice - 1]);
            System.out.println("Items sorted successfully.");
        } else {
            System.out.println("Please choose a sort field from 1 to 5.");
            return;
        }
        printItems(service.getItems());
    }

    private static void viewReservationQueue(Scanner scanner, LibraryService service) {
        String itemID = readNonEmptyString(scanner, "Item ID: ");
        try {
            List<String> queue = service.getReservationQueue(itemID);
            System.out.println(queue.isEmpty() ? "Reservation queue is empty." : "Queue: " + String.join(" -> ", queue));
        } catch (ItemNotAvailableException | IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static String clip(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width - 3) + "...";
    }

    private static void printHelp() {
        printSection("QUICK HELP");
        System.out.println("Use Catalog to find, filter, and sort items before borrowing.");
        System.out.println("A returned item can have its fine paid in full or in partial payments.");
        System.out.println("Reservations are FIFO; cancel your reservation from Transaction Workflows.");
        System.out.println("Save data after important changes, or choose Save and exit.");
    }

    private static void addItem(Scanner scanner, LibraryService libraryService) {
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        String title = readNonEmptyString(scanner, "Enter item title: ");
        String type = readNonEmptyString(scanner, "Enter type (Book, EBook, Journal): ");
        System.out.print("Enter author (optional): ");
        String author = scanner.nextLine().trim();

        try {
            libraryService.addItemWithDetails(itemID, title, author, type);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void addUser(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String name = readNonEmptyString(scanner, "Enter user name: ");
        String email = readOptionalString(scanner, "Enter email (optional, leave blank to skip): ");

        try {
            libraryService.addUser(userID, name, email);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void searchUser(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID to search: ");
        libraryService.searchUserById(userID);
    }

    private static void issueItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        LocalDate issueDate = readDate(scanner, "Enter issue date (YYYY-MM-DD): ");

        try {
            libraryService.issueItem(userID, itemID, issueDate);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void returnItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        LocalDate returnDate = readDate(scanner, "Enter return date (YYYY-MM-DD): ");

        try {
            libraryService.returnItem(userID, itemID, returnDate);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (OverdueException ex) {
            System.out.println("Return complete with overdue fine. " + ex.getMessage());
        }
    }

    private static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ex) {
                System.out.println("Please enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

    /**
     * Reservation queues are in-memory session state only.
     * They are intentionally not saved to CSV and are cleared when a new LibraryService
     * loads data from disk for a fresh application session.
     */
    private static void reserveItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");

        try {
            libraryService.reserveItem(userID, itemID);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void viewBorrowingHistory(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        for (Transaction transaction : libraryService.getBorrowingHistoryForUser(userID)) {
            System.out.println(transaction);
        }
    }

    private static void recordFinePayment(Scanner scanner, LibraryService libraryService) {
        String transactionID = readNonEmptyString(scanner, "Enter transaction ID: ");
        double amount;
        while (true) {
            try {
                amount = Double.parseDouble(readNonEmptyString(scanner, "Enter payment amount: "));
                break;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid payment amount.");
            }
        }
        LocalDate paymentDate = readDate(scanner, "Enter payment date (YYYY-MM-DD): ");
        try {
            libraryService.recordFinePayment(transactionID, amount, paymentDate);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void cancelReservation(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        try {
            if (!libraryService.cancelReservation(userID, itemID)) {
                System.out.println("No matching reservation found.");
            }
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void checkDataIntegrity(LibraryService libraryService) {
        java.util.List<String> issues = libraryService.validateDataIntegrity();
        if (issues.isEmpty()) {
            System.out.println("Data integrity check passed.");
            return;
        }
        System.out.println("Data integrity issues found:");
        for (String issue : issues) {
            System.out.println("- " + issue);
        }
    }
}
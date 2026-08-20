package main;

import exceptions.InvalidUserException;
import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;
import services.LibraryService;

import java.util.Scanner;

/**
 * Command-line launcher for the Smart Library Management System.
 *
 * <p>
 * This class provides a simple menu-driven interface for users to manage
 * library items, library members, and library transactions.</p>
 */
public class Main {
    public static void main(String[] args) {
        String dataDirectory = getDataDirectory();
        LibraryService libraryService = new LibraryService(dataDirectory);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInteger(scanner, "Enter your choice: ");

            switch (choice) {
                case 1:
                    addItem(scanner, libraryService);
                    break;
                case 2:
                    addUser(scanner, libraryService);
                    break;
                case 3:
                    libraryService.viewItems();
                    break;
                case 4:
                    searchItem(scanner, libraryService);
                    break;
                case 5:
                    issueItem(scanner, libraryService);
                    break;
                case 6:
                    returnItem(scanner, libraryService);
                    break;
                case 7:
                    reserveItem(scanner, libraryService);
                    break;
                case 8:
                    libraryService.sortItems();
                    libraryService.viewItems();
                    break;
                case 9:
                    libraryService.showReports();
                    break;
                case 10:
                    libraryService.undoLastTransaction();
                    break;
                case 11:
                    libraryService.saveData();
                    break;
                case 12:
                    libraryService.saveData();
                    running = false;
                    System.out.println("Exiting the system. Goodbye!");
                    break;
                default:
                    System.out.println("Please enter a valid menu option.");
                    break;
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("************************************");
        System.out.println("SMART LIBRARY MANAGEMENT SYSTEM");
        System.out.println("************************************");
        System.out.println("1. Add Item");
        System.out.println("2. Add User");
        System.out.println("3. View Items");
        System.out.println("4. Search Item");
        System.out.println("5. Issue Item");
        System.out.println("6. Return Item");
        System.out.println("7. Reserve Item");
        System.out.println("8. Sort Items");
        System.out.println("9. Show Reports");
        System.out.println("10. Undo Last Action");
        System.out.println("11. Save Data");
        System.out.println("12. Exit");
        System.out.println("************************************");
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

    private static int readPositiveInteger(Scanner scanner, String prompt) {
        while (true) {
            int value = readInteger(scanner, prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Please enter a positive integer.");
        }
    }

    private static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static String getDataDirectory() {
        String dataDirectory = System.getenv("LIBRARY_DATA_DIR");
        return (dataDirectory == null || dataDirectory.trim().isEmpty()) ? "data" : dataDirectory.trim();
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

        try {
            libraryService.addUser(userID, name);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void searchItem(Scanner scanner, LibraryService libraryService) {
        System.out.println("Search by: 1) ID  2) Title");
        int option = readInteger(scanner, "Enter option: ");
        if (option == 1) {
            String itemID = readNonEmptyString(scanner, "Enter item ID: ");
            libraryService.searchItemById(itemID);
        } else if (option == 2) {
            String keyword = readNonEmptyString(scanner, "Enter title keyword: ");
            libraryService.searchItemsByTitle(keyword);
        } else {
            System.out.println("Please choose one of the given options.");
        }
    }

    private static void issueItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        int issueDay = readPositiveInteger(scanner, "Enter issue day (positive integer): ");

        try {
            libraryService.issueItem(userID, itemID, issueDay);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void returnItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");
        int returnDay = readPositiveInteger(scanner, "Enter return day (positive integer): ");

        try {
            libraryService.returnItem(userID, itemID, returnDay);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (OverdueException ex) {
            System.out.println("Return complete with overdue fine. " + ex.getMessage());
        }
    }

    private static void reserveItem(Scanner scanner, LibraryService libraryService) {
        String userID = readNonEmptyString(scanner, "Enter user ID: ");
        String itemID = readNonEmptyString(scanner, "Enter item ID: ");

        try {
            libraryService.reserveItem(userID, itemID);
        } catch (InvalidUserException | ItemNotAvailableException | IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
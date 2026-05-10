package main;

import exceptions.InvalidUserException;
import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;
import services.LibraryService;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        LibraryService libraryService = new LibraryService("data");
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
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("====================================");
        System.out.println("SMART LIBRARY MANAGEMENT SYSTEM");
        System.out.println("NIT Library System");
        System.out.println("====================================");
        System.out.println("1. Add Item");
        System.out.println("2. Add User");
        System.out.println("3. View Items");
        System.out.println("4. Search Item");
        System.out.println("5. Issue Item");
        System.out.println("6. Return Item");
        System.out.println("7. Reserve Item");
        System.out.println("8. Sort Items");
        System.out.println("9. Show Reports");
        System.out.println("10. Undo Last Transaction");
        System.out.println("11. Save Data");
        System.out.println("12. Exit");
        System.out.println("====================================");
    }

    private static int readInteger(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static void addItem(Scanner scanner, LibraryService libraryService) {
        System.out.print("Enter item ID: ");
        String itemID = scanner.nextLine().trim();
        System.out.print("Enter item title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter type (Book, EBook, Journal): ");
        String type = scanner.nextLine().trim();

        libraryService.addItem(itemID, title, type);
    }

    private static void addUser(Scanner scanner, LibraryService libraryService) {
        System.out.print("Enter user ID: ");
        String userID = scanner.nextLine().trim();
        System.out.print("Enter user name: ");
        String name = scanner.nextLine().trim();

        libraryService.addUser(userID, name);
    }

    private static void searchItem(Scanner scanner, LibraryService libraryService) {
        System.out.println("Search by: 1) ID  2) Title");
        int option = readInteger(scanner, "Enter option: ");
        if (option == 1) {
            System.out.print("Enter item ID: ");
            String itemID = scanner.nextLine().trim();
            libraryService.searchItemById(itemID);
        } else if (option == 2) {
            System.out.print("Enter title keyword: ");
            String keyword = scanner.nextLine().trim();
            libraryService.searchItemsByTitle(keyword);
        } else {
            System.out.println("Invalid search option.");
        }
    }

    private static void issueItem(Scanner scanner, LibraryService libraryService) {
        System.out.print("Enter user ID: ");
        String userID = scanner.nextLine().trim();
        System.out.print("Enter item ID: ");
        String itemID = scanner.nextLine().trim();
        int issueDay = readInteger(scanner, "Enter issue day (integer): ");

        try {
            libraryService.issueItem(userID, itemID, issueDay);
        } catch (InvalidUserException | ItemNotAvailableException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void returnItem(Scanner scanner, LibraryService libraryService) {
        System.out.print("Enter user ID: ");
        String userID = scanner.nextLine().trim();
        System.out.print("Enter item ID: ");
        String itemID = scanner.nextLine().trim();
        int returnDay = readInteger(scanner, "Enter return day (integer): ");

        try {
            libraryService.returnItem(userID, itemID, returnDay);
        } catch (InvalidUserException | ItemNotAvailableException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (OverdueException ex) {
            System.out.println("Return complete with overdue fine. " + ex.getMessage());
        }
    }

    private static void reserveItem(Scanner scanner, LibraryService libraryService) {
        System.out.print("Enter user ID: ");
        String userID = scanner.nextLine().trim();
        System.out.print("Enter item ID: ");
        String itemID = scanner.nextLine().trim();

        try {
            libraryService.reserveItem(userID, itemID);
        } catch (InvalidUserException | ItemNotAvailableException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}

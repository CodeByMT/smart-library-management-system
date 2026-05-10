package services;

import exceptions.InvalidUserException;
import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;
import models.Book;
import models.EBook;
import models.Journal;
import models.LibraryItem;
import models.Transaction;
import models.User;
import utils.SearchUtil;
import utils.SortUtil;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class LibraryService {
    private final ArrayList<LibraryItem> items;
    private final ArrayList<User> users;
    private final ArrayList<Transaction> transactions;
    private final Stack<UndoRecord> undoStack;
    private final String dataFolder;

    public LibraryService(String dataFolder) {
        this.dataFolder = dataFolder;
        FileManager.ensureDataFiles(dataFolder);
        this.items = FileManager.loadItems(dataFolder + "/books.csv");
        this.users = FileManager.loadUsers(dataFolder + "/users.csv");
        this.transactions = FileManager.loadTransactions(dataFolder + "/transactions.csv");
        this.undoStack = new Stack<>();
        restoreItemAvailabilityFromTransactions();
    }

    public ArrayList<LibraryItem> getItems() {
        return items;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addItem(String itemID, String title, String type) {
        if (findItemById(itemID) != null) {
            System.out.println("Item ID already exists.");
            return;
        }

        LibraryItem item;
        switch (type.toLowerCase()) {
            case "book":
                item = new Book(itemID, title, true);
                break;
            case "ebook":
                item = new EBook(itemID, title, true);
                break;
            case "journal":
                item = new Journal(itemID, title, true);
                break;
            default:
                System.out.println("Unsupported item type. Choose Book, EBook or Journal.");
                return;
        }

        items.add(item);
        System.out.println("Item added successfully.");
    }

    public void addUser(String userID, String name) {
        if (findUserById(userID) != null) {
            System.out.println("User ID already exists.");
            return;
        }

        users.add(new User(userID, name));
        System.out.println("User added successfully.");
    }

    public void viewItems() {
        if (items.isEmpty()) {
            System.out.println("No items are registered yet.");
            return;
        }

        for (LibraryItem item : items) {
            item.displayInfo();
        }
    }

    public void searchItemById(String id) {
        LibraryItem item = SearchUtil.searchByID(items, id);
        if (item == null) {
            System.out.println("No item found with ID " + id);
        } else {
            item.displayInfo();
        }
    }

    public void searchItemsByTitle(String keyword) {
        SearchUtil.searchByTitle(items, keyword);
    }

    public void sortItems() {
        SortUtil.bubbleSortByTitle(items);
    }

    public void issueItem(String userID, String itemID, int issueDay) throws InvalidUserException, ItemNotAvailableException {
        User user = findUserById(userID);
        if (user == null) {
            throw new InvalidUserException("User ID not found: " + userID);
        }

        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }

        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Item is currently issued to another user.");
        }

        String reservedUserId = item.peekReservation();
        if (reservedUserId != null && !reservedUserId.equalsIgnoreCase(userID)) {
            throw new ItemNotAvailableException("Item is reserved by another user.");
        }

        boolean removedReservation = false;
        if (reservedUserId != null && reservedUserId.equalsIgnoreCase(userID)) {
            item.pollReservation();
            removedReservation = true;
        }

        item.issueItem(user);
        user.borrowItem(item);
        String transactionId = buildTransactionId();
        int dueDay = issueDay + 7;
        Transaction transaction = new Transaction(transactionId, userID, itemID, issueDay, dueDay, -1, 0.0);
        transactions.add(transaction);
        undoStack.push(new UndoRecord("ISSUE", transaction, itemID, userID, removedReservation));

        System.out.println("Item issued successfully. Due day: " + dueDay);
    }

    public void returnItem(String userID, String itemID, int returnDay) throws InvalidUserException, ItemNotAvailableException, OverdueException {
        User user = findUserById(userID);
        if (user == null) {
            throw new InvalidUserException("User ID not found: " + userID);
        }

        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }

        Transaction transaction = findActiveTransaction(itemID, userID);
        if (transaction == null) {
            throw new ItemNotAvailableException("No active issue transaction found for this item and user.");
        }

        item.returnItem(user);
        user.returnBorrowedItem(item);
        transaction.setReturnDay(returnDay);

        int daysLate = Math.max(0, returnDay - transaction.getDueDay());
        double fine = 0.0;
        if (daysLate > 0) {
            fine = item.calculateFine(daysLate);
            transaction.setFine(fine);
        }

        undoStack.push(new UndoRecord("RETURN", transaction, itemID, userID, false));
        System.out.println("Item returned successfully.");
        if (fine > 0) {
            throw new OverdueException("Overdue by " + daysLate + " days. Fine to pay: Rs " + (int) fine);
        }
    }

    public void reserveItem(String userID, String itemID) throws InvalidUserException, ItemNotAvailableException {
        User user = findUserById(userID);
        if (user == null) {
            throw new InvalidUserException("User ID not found: " + userID);
        }

        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }

        if (!item.reserveItem(userID)) {
            System.out.println("You have already reserved this item.");
            return;
        }

        undoStack.push(new UndoRecord("RESERVE", null, itemID, userID, false));
        System.out.println("Reservation added. Current queue: " + item.getReservationList());
    }

    public void saveData() {
        FileManager.saveItems(dataFolder + "/books.csv", items);
        FileManager.saveUsers(dataFolder + "/users.csv", users);
        FileManager.saveTransactions(dataFolder + "/transactions.csv", transactions);
        System.out.println("Data saved successfully.");
    }

    public void showReports() {
        long totalItems = items.size();
        long availableItems = items.stream().filter(LibraryItem::isAvailable).count();
        long issuedItems = totalItems - availableItems;

        LibraryItem mostBorrowed = null;
        for (LibraryItem item : items) {
            if (mostBorrowed == null || item.getBorrowCount() > mostBorrowed.getBorrowCount()) {
                mostBorrowed = item;
            }
        }

        System.out.println("--- LIBRARY REPORT ---");
        System.out.println("Total items: " + totalItems);
        System.out.println("Available items: " + availableItems);
        System.out.println("Issued items: " + issuedItems);
        System.out.println("Total users: " + users.size());
        System.out.println("Most borrowed item: " + (mostBorrowed != null ? mostBorrowed.getTitle() + " (" + mostBorrowed.getBorrowCount() + " times)" : "None"));
    }

    public boolean undoLastTransaction() {
        if (undoStack.isEmpty()) {
            System.out.println("No actions available to undo.");
            return false;
        }

        UndoRecord undo = undoStack.pop();
        String action = undo.action;
        Transaction transaction = undo.transaction;
        String itemID = undo.itemId;
        String userID = undo.userId;
        LibraryItem item = findItemById(itemID);
        User user = findUserById(userID);

        switch (action) {
            case "ISSUE":
                if (transaction != null && item != null && user != null) {
                    item.setAvailable(true);
                    user.returnBorrowedItem(item);
                    transactions.remove(transaction);
                    if (undo.reservationRemoved) {
                        item.reserveItem(userID);
                    }
                    System.out.println("Undo successful: issue transaction reversed.");
                    return true;
                }
                break;
            case "RETURN":
                if (transaction != null && item != null && user != null) {
                    item.setAvailable(false);
                    user.borrowItem(item);
                    transaction.setReturnDay(-1);
                    transaction.setFine(0.0);
                    System.out.println("Undo successful: return transaction reversed.");
                    return true;
                }
                break;
            case "RESERVE":
                if (item != null) {
                    if (item.removeReservation(userID)) {
                        System.out.println("Undo successful: reservation removed.");
                        return true;
                    }
                }
                break;
            default:
                break;
        }

        System.out.println("Undo action failed.");
        return false;
    }

    private LibraryItem findItemById(String itemID) {
        for (LibraryItem item : items) {
            if (item.getItemID().equalsIgnoreCase(itemID)) {
                return item;
            }
        }
        return null;
    }

    private User findUserById(String userID) {
        for (User user : users) {
            if (user.getId().equalsIgnoreCase(userID)) {
                return user;
            }
        }
        return null;
    }

    private Transaction findActiveTransaction(String itemID, String userID) {
        for (Transaction transaction : transactions) {
            if (transaction.getItemID().equalsIgnoreCase(itemID)
                    && transaction.getUserID().equalsIgnoreCase(userID)
                    && !transaction.isReturned()) {
                return transaction;
            }
        }
        return null;
    }

    private void restoreItemAvailabilityFromTransactions() {
        for (Transaction transaction : transactions) {
            if (!transaction.isReturned()) {
                LibraryItem item = findItemById(transaction.getItemID());
                if (item != null) {
                    item.setAvailable(false);
                }
            }
        }
    }

    private String buildTransactionId() {
        int maxId = 0;
        for (Transaction transaction : transactions) {
            String raw = transaction.getTransactionID().replaceAll("[^0-9]", "");
            if (!raw.isEmpty()) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(raw));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("T%03d", maxId + 1);
    }

    private static class UndoRecord {
        private final String action;
        private final Transaction transaction;
        private final String itemId;
        private final String userId;
        private final boolean reservationRemoved;

        public UndoRecord(String action, Transaction transaction, String itemId, String userId, boolean reservationRemoved) {
            this.action = action;
            this.transaction = transaction;
            this.itemId = itemId;
            this.userId = userId;
            this.reservationRemoved = reservationRemoved;
        }
    }
}

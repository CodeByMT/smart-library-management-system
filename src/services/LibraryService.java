package services;

import exceptions.InvalidUserException;
import exceptions.ItemNotAvailableException;
import exceptions.OverdueException;
import models.Book;
import models.EBook;
import models.Journal;
import models.LibraryItem;
import models.Transaction;
import models.TransactionAction;
import models.UndoRecord;
import models.User;
import utils.InputValidator;
import utils.SearchUtil;
import utils.SortUtil;

import java.util.stream.Collectors;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Core service layer for library operations.
 *
 * <p>This class manages library items, users, transactions, and undo actions.
 * It coordinates all business logic for the library management system and ensures
 * data consistency across operations. All public methods validate inputs and 
 * maintain an undo stack for transaction reversal.</p>
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Item management (add, search, sort)</li>
 *   <li>User management (registration)</li>
 *   <li>Transaction processing (issue, return, reserve)</li>
 *   <li>Undo/rollback functionality</li>
 *   <li>Data persistence coordination</li>

 *   <li>Library reporting</li>
 * </ul>
 */
public class LibraryService {
    private static final long LOAN_PERIOD_DAYS = 7;

    private final ArrayList<LibraryItem> items;
    private final ArrayList<User> users;
    private final ArrayList<Transaction> transactions;
    private final Map<String, LibraryItem> itemsById;
    private final Map<String, User> usersById;
    private final ArrayDeque<UndoRecord> undoStack;
    private final String dataFolder;

    public LibraryService(String dataFolder) {
        this.dataFolder = dataFolder;
        FileManager.ensureDataFiles(dataFolder);
        this.items = FileManager.loadItems(dataFolder + "/books.csv");
        this.users = FileManager.loadUsers(dataFolder + "/users.csv");
        this.transactions = FileManager.loadTransactions(dataFolder + "/transactions.csv");
        this.itemsById = new HashMap<>();
        this.usersById = new HashMap<>();
        this.undoStack = new ArrayDeque<>();
        indexLoadedEntities();
        rebuildBorrowCountsFromTransactions();
        restoreItemAvailabilityFromTransactions();
        restoreBorrowedItemsFromTransactions();
    }

    public List<LibraryItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getBorrowingHistoryForUser(String userID) {
        if (userID == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(transactions.stream()
                .filter(transaction -> transaction.getUserID().equalsIgnoreCase(userID.trim()))
                .collect(Collectors.toList()));
    }

    public List<Transaction> getBorrowingHistoryForItem(String itemID) {
        if (itemID == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(transactions.stream()
                .filter(transaction -> transaction.getItemID().equalsIgnoreCase(itemID.trim()))
                .collect(Collectors.toList()));
    }

    public void addItem(String itemID, String title, String type) {
        addItemWithDetails(itemID, title, "", type);
    }

    /**
     * Adds a new library item with detailed information.
     * Validates all inputs and ensures unique item IDs.
     *
     * @param itemID unique identifier for the item (cannot be empty)
     * @param title the title of the item (cannot be empty)
     * @param author the author name (can be empty)
     * @param type the item type: "Book", "EBook", or "Journal"
     * @throws IllegalArgumentException if validation fails or ID already exists
     */
    public void addItemWithDetails(String itemID, String title, String author, String type) {
        // Validate inputs using centralized validator
        InputValidator.validateNonEmpty(itemID, "Item ID");
        InputValidator.validateNonEmpty(title, "Item title");
        InputValidator.validateNonEmpty(type, "Item type");
        
        if (findItemById(itemID) != null) {
            throw new IllegalArgumentException("Item ID already exists: " + itemID);
        }

        LibraryItem item;
        switch (type.toLowerCase()) {
            case "book":
                item = new Book(itemID, title, author, true);
                break;
            case "ebook":
                item = new EBook(itemID, title, author, true);
                break;
            case "journal":
                item = new Journal(itemID, title, author, true);
                break;
            default:
                throw new IllegalArgumentException("Unsupported item type. Choose Book, EBook or Journal.");
        }

        items.add(item);
        itemsById.put(normalizeId(itemID), item);
        System.out.println("Item added successfully.");
    }

    /**
     * Registers a new user in the library system.
     * Validates inputs and ensures unique user IDs.
     *
     * @param userID unique identifier for the user (cannot be empty)
     * @param name the name of the user (cannot be empty)
     * @throws IllegalArgumentException if validation fails or ID already exists
     */
    public void addUser(String userID, String name) {
        addUser(userID, name, "");
    }

    public void addUser(String userID, String name, String email) {
        InputValidator.validateNonEmpty(userID, "User ID");
        InputValidator.validateNonEmpty(name, "User name");
        InputValidator.validateOptionalEmail(email);

        if (findUserById(userID) != null) {
            throw new IllegalArgumentException("User ID already exists: " + userID);
        }

        User user = new User(userID, name, email);
        users.add(user);
        usersById.put(normalizeId(userID), user);
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

    public void viewUsers() {
        if (users.isEmpty()) {
            System.out.println("No users are registered yet.");
            return;
        }

        System.out.println("\nRegistered users:");
        for (User user : users) {
            user.displayInfo();
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

    public void searchUserById(String userID) {
        if (userID == null || userID.trim().isEmpty()) {
            System.out.println("Please provide a non-empty user ID.");
            return;
        }

        User user = findUserById(userID);
        if (user == null) {
            System.out.println("No user found with ID " + userID);
        } else {
            user.displayInfo();
        }
    }

    public void searchItemsByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Please provide a non-empty title keyword.");
            return;
        }
        SearchUtil.searchByTitle(items, keyword);
    }

    public List<LibraryItem> searchItems(String keyword) {
        return Collections.unmodifiableList(SearchUtil.searchByKeyword(items, keyword));
    }

    public List<LibraryItem> filterItems(String type, Boolean available) {
        Set<String> types = new HashSet<>();
        if (type != null) {
            types.add(type);
        }
        return filterItems(types, available);
    }

    public List<LibraryItem> filterItems(Set<String> types, Boolean available) {
        return Collections.unmodifiableList(SearchUtil.filter(items, types, available));
    }

    public void sortItems() {
        SortUtil.bubbleSortByTitle(items);
    }

    public void sortItemsBy(SortUtil.SortOption option) {
        SortUtil.sort(items, option);
    }

    /**
     * Issues a library item using the legacy day-number API.
     * 
     * <p>Validates availability and user borrow limits. Removes the user from the
     * reservation queue if they are first in line. Creates a transaction record
     * and pushes an undo record to the stack.</p>
     *
     * @param userID the ID of the user borrowing the item
     * @param itemID the ID of the item to borrow
    * @param issueDay the legacy day number on which the item is issued
     * @throws InvalidUserException if the user ID is not found
     * @throws ItemNotAvailableException if the item is not found, not available, user has reached borrow limit,
     *                                  or item is reserved by another user
     * @throws IllegalArgumentException if issueDay is not positive
     * @throws IllegalStateException if borrowing fails internally
     */
    public void issueItem(String userID, String itemID, int issueDay) 
            throws InvalidUserException, ItemNotAvailableException {
        InputValidator.validatePositive(issueDay, "Issue day");
        issueItem(userID, itemID, legacyDayToDate(issueDay));
    }

    /**
     * Issues a library item using a calendar date.
     *
     * @param issueDate the calendar date on which the item is issued
     */
    public void issueItem(String userID, String itemID, LocalDate issueDate)
            throws InvalidUserException, ItemNotAvailableException {
        InputValidator.validateDate(issueDate, "Issue date");
        
        User user = findUserById(userID);
        if (user == null) {
            throw new InvalidUserException("User ID not found: " + userID);
        }

        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }

        validateItemAvailabilityForIssue(item, user);
        
        // Handle reservation: if user is first in queue, remove them
        boolean removedReservation = handleReservationForIssue(item, userID);

        // Execute the issue operation
        item.issueItem(user);
        if (!user.borrowItem(item)) {
            throw new IllegalStateException("Unable to record borrowed item for user " + userID);
        }
        
        // Create and record the transaction
        String transactionId = buildTransactionId();
        LocalDate dueDate = issueDate.plusDays(LOAN_PERIOD_DAYS);
        Transaction transaction = new Transaction(transactionId, userID, itemID, issueDate, dueDate, null, 0.0);
        transactions.add(transaction);
        
        // Record undo information
        undoStack.push(new UndoRecord(TransactionAction.ISSUE, transaction, itemID, userID, removedReservation));

        System.out.println("Item issued successfully. Due date: " + dueDate);
    }
    
    /**
     * Validates that an item can be issued to a user.
     * @param item the item to validate
     * @param user the user requesting the item
     * @throws ItemNotAvailableException if item or user conditions are not met
     */
    private void validateItemAvailabilityForIssue(LibraryItem item, User user) throws ItemNotAvailableException {
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Item is currently issued to another user.");
        }

        if (!user.canBorrow()) {
            throw new ItemNotAvailableException("User has reached maximum borrow limit.");
        }

        String reservedUserId = item.peekReservation();
        if (reservedUserId != null && !reservedUserId.equalsIgnoreCase(user.getId())) {
            throw new ItemNotAvailableException("Item is reserved by another user.");
        }
    }
    
    /**
     * Handles reservation check before issuing. If the user is first in the
     * reservation queue, removes them from the queue.
     * @param item the item being issued
     * @param userID the ID of the user receiving the item
     * @return true if user was in reservation queue and removed, false otherwise
     */
    private boolean handleReservationForIssue(LibraryItem item, String userID) {
        String reservedUserId = item.peekReservation();
        if (reservedUserId != null && reservedUserId.equalsIgnoreCase(userID)) {
            item.pollReservation();
            return true;
        }
        return false;
    }

    /**
     * Processes a return using the legacy day-number API.
     * 
     * <p>Validates the return operation, calculates any overdue fines, and updates
     * transaction records. If overdue fines apply, throws OverdueException with
     * fine details (note: operation is still completed).</p>
     *
     * @param userID the ID of the user returning the item
     * @param itemID the ID of the item being returned
    * @param returnDay the legacy day number on which the item is returned
     * @throws InvalidUserException if the user ID is not found
     * @throws ItemNotAvailableException if the item is not found or no active transaction exists
     * @throws OverdueException if the return is late (item is still returned, but exception carries fine info)
     * @throws IllegalArgumentException if returnDay is invalid
     */
    public void returnItem(String userID, String itemID, int returnDay) 
            throws InvalidUserException, ItemNotAvailableException, OverdueException {
        InputValidator.validatePositive(returnDay, "Return day");
        returnItem(userID, itemID, legacyDayToDate(returnDay));
    }

    /**
     * Processes a return using a calendar date.
     *
     * @param returnDate the calendar date on which the item is returned
     */
    public void returnItem(String userID, String itemID, LocalDate returnDate)
            throws InvalidUserException, ItemNotAvailableException, OverdueException {
        InputValidator.validateDate(returnDate, "Return date");
        
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

        InputValidator.validateDateSequence(returnDate, transaction.getIssueDate(),
                          "Return date", "Issue date");

        // Process the return
        item.returnItem(user);
        user.returnBorrowedItem(item);
        transaction.setReturnDate(returnDate);

        // Calculate fine if overdue
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(transaction.getDueDate(), returnDate));
        double fine = 0.0;
        if (daysLate > 0) {
            fine = item.calculateFine(Math.toIntExact(daysLate));
            transaction.setFine(fine);
        }

        // Record undo information
        undoStack.push(new UndoRecord(TransactionAction.RETURN, transaction, itemID, userID, false));
        System.out.println("Item returned successfully.");
        
        // Throw exception if overdue (but operation is complete)
        if (fine > 0) {
            throw new OverdueException("Overdue by " + daysLate + " days. Fine to pay: Rs " + (int) fine);
        }
    }

    /**
     * Reserves a library item for a user.
     * 
     * <p>Adds the user to the reservation queue for an item. Users can only be in
     * the queue once per item. Items should typically be reserved only when not available.</p>
     *
     * @param userID the ID of the user making the reservation
     * @param itemID the ID of the item to reserve
     * @throws InvalidUserException if the user ID is not found
     * @throws ItemNotAvailableException if the item ID is not found
     */
    /**
     * Creates a reservation in memory for the current session only.
     * Reservation state is intentionally not persisted to CSV and is cleared when the
     * application loads fresh data through a new LibraryService instance.
     */
    public void reserveItem(String userID, String itemID) throws InvalidUserException, ItemNotAvailableException {
        InputValidator.validateNonEmpty(userID, "User ID");
        InputValidator.validateNonEmpty(itemID, "Item ID");

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

        undoStack.push(new UndoRecord(TransactionAction.RESERVE, null, itemID, userID, false));
        System.out.println("Reservation added. Current queue: " + item.getReservationList());
    }

    public boolean cancelReservation(String userID, String itemID)
            throws InvalidUserException, ItemNotAvailableException {
        InputValidator.validateNonEmpty(userID, "User ID");
        InputValidator.validateNonEmpty(itemID, "Item ID");
        if (findUserById(userID) == null) {
            throw new InvalidUserException("User ID not found: " + userID);
        }
        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }
        boolean removed = item.removeReservation(userID);
        if (removed) {
            System.out.println("Reservation cancelled.");
        }
        return removed;
    }

    public List<String> getReservationQueue(String itemID) throws ItemNotAvailableException {
        InputValidator.validateNonEmpty(itemID, "Item ID");
        LibraryItem item = findItemById(itemID);
        if (item == null) {
            throw new ItemNotAvailableException("Item ID not found: " + itemID);
        }
        return item.getReservationQueue();
    }

    public void recordFinePayment(String transactionID, double amount, LocalDate paymentDate) {
        InputValidator.validateNonEmpty(transactionID, "Transaction ID");
        InputValidator.validateDate(paymentDate, "Payment date");
        Transaction transaction = findTransactionById(transactionID);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction ID not found: " + transactionID);
        }
        if (!transaction.isReturned()) {
            throw new IllegalStateException("Fine payments can only be recorded after an item is returned.");
        }
        transaction.recordPayment(amount, paymentDate);
        System.out.println("Fine payment recorded successfully.");
    }

    public void saveData() {
        FileManager.saveItems(dataFolder + "/books.csv", items);
        FileManager.saveUsers(dataFolder + "/users.csv", users);
        FileManager.saveTransactions(dataFolder + "/transactions.csv", transactions);
        System.out.println("Data saved successfully.");
    }

    /**
     * Reverses the last transaction (issue, return, or reservation).
     * 
     * <p>Pops the undo stack and reverses the corresponding operation:
     * <ul>
     *   <li>ISSUE: Makes item available again, removes from user's borrowed items</li>
     *   <li>RETURN: Marks item as borrowed again, restores to user's list</li>
     *   <li>RESERVE: Removes user from reservation queue</li>
     * </ul>
     * </p>
     *
     * @return true if undo was successful, false if no undo history or undo failed
     */
    public boolean undoLastTransaction() {
        if (undoStack.isEmpty()) {
            System.out.println("No actions available to undo.");
            return false;
        }

        UndoRecord undo = undoStack.pop();
        TransactionAction action = undo.getAction();
        Transaction transaction = undo.getTransaction();
        String itemID = undo.getItemId();
        String userID = undo.getUserId();
        
        LibraryItem item = findItemById(itemID);
        User user = findUserById(userID);

        switch (action) {
            case ISSUE:
                return undoIssueTransaction(item, user, transaction, undo);
                
            case RETURN:
                return undoReturnTransaction(item, user, transaction);
                
            case RESERVE:
                return undoReserveTransaction(item, userID);
                
            default:
                System.out.println("Unknown undo action: " + action);
                return false;
        }
    }
    
    /**
     * Reverses an ISSUE transaction.
     */
    private boolean undoIssueTransaction(LibraryItem item, User user, Transaction transaction, UndoRecord undo) {
        if (transaction != null && item != null && user != null) {
            item.setAvailable(true);
            item.decrementBorrowCount();
            user.returnBorrowedItem(item);
            transactions.remove(transaction);

            // Restore reservation if one was removed during issue
            if (undo.isReservationRemoved()) {
                item.restoreReservationAtFront(undo.getUserId());
            }

            System.out.println("Undo successful: issue transaction reversed.");
            return true;
        }
        System.out.println("Undo action failed: missing transaction, item, or user data.");
        return false;
    }
    
    /**
     * Reverses a RETURN transaction.
     */
    private boolean undoReturnTransaction(LibraryItem item, User user, Transaction transaction) {
        if (transaction != null && item != null && user != null) {
            item.setAvailable(false);
            user.borrowItem(item);
            transaction.setReturnDate(null);
            transaction.setFine(0.0);
            System.out.println("Undo successful: return transaction reversed.");
            return true;
        }
        System.out.println("Undo action failed: missing transaction, item, or user data.");
        return false;
    }
    
    /**
     * Reverses a RESERVE transaction.
     */
    private boolean undoReserveTransaction(LibraryItem item, String userID) {
        if (item != null) {
            if (item.removeReservation(userID)) {
                System.out.println("Undo successful: reservation removed.");
                return true;
            }
        }
        System.out.println("Undo action failed: could not remove reservation.");
        return false;
    }

    private LibraryItem findItemById(String itemID) {
        return itemID == null ? null : itemsById.get(normalizeId(itemID));
    }

    private User findUserById(String userID) {
        return userID == null ? null : usersById.get(normalizeId(userID));
    }

    private Transaction findTransactionById(String transactionID) {
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionID().equalsIgnoreCase(transactionID.trim())) {
                return transaction;
            }
        }
        return null;
    }

    private void indexLoadedEntities() {
        for (LibraryItem item : items) {
            itemsById.put(normalizeId(item.getItemID()), item);
        }
        for (User user : users) {
            usersById.put(normalizeId(user.getId()), user);
        }
    }

    private String normalizeId(String id) {
        return id.toLowerCase(Locale.ROOT);
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

    /**
     * Reconstructs each item's historical issue count from the transaction history.
     * This deliberately counts successful issue events, not currently active borrows.
     * Active availability is derived separately from item availability and return state.
     */
    private void rebuildBorrowCountsFromTransactions() {
        for (LibraryItem item : items) {
            item.resetBorrowCount();
        }

        for (Transaction transaction : transactions) {
            LibraryItem item = findItemById(transaction.getItemID());
            if (item != null) {
                item.incrementBorrowCount();
            }
        }
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

    private void restoreBorrowedItemsFromTransactions() {
        for (Transaction transaction : transactions) {
            if (!transaction.isReturned()) {
                User user = findUserById(transaction.getUserID());
                LibraryItem item = findItemById(transaction.getItemID());
                if (user != null && item != null) {
                    user.borrowItem(item);
                }
            }
        }
    }

    public List<String> validateDataIntegrity() {
        List<String> issues = new ArrayList<>();
        Set<String> itemIDs = new HashSet<>();
        for (LibraryItem item : items) {
            if (!itemIDs.add(normalizeId(item.getItemID()))) {
                issues.add("Duplicate item ID: " + item.getItemID());
            }
        }
        Set<String> userIDs = new HashSet<>();
        for (User user : users) {
            if (!userIDs.add(normalizeId(user.getId()))) {
                issues.add("Duplicate user ID: " + user.getId());
            }
        }
        Set<String> activeItems = new HashSet<>();
        for (Transaction transaction : transactions) {
            if (findItemById(transaction.getItemID()) == null) {
                issues.add("Transaction references missing item: " + transaction.getItemID());
            }
            if (findUserById(transaction.getUserID()) == null) {
                issues.add("Transaction references missing user: " + transaction.getUserID());
            }
            if (!transaction.isReturned() && !activeItems.add(normalizeId(transaction.getItemID()))) {
                issues.add("Multiple active transactions for item: " + transaction.getItemID());
            }
        }
        for (LibraryItem item : items) {
            boolean active = activeItems.contains(normalizeId(item.getItemID()));
            if (active == item.isAvailable()) {
                issues.add("Availability mismatch for item: " + item.getItemID());
            }
        }
        return Collections.unmodifiableList(issues);
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

    private String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    public void showReports() {
        showReports(LocalDate.now());
    }

    public void showReports(LocalDate asOfDate) {
        InputValidator.validateDate(asOfDate, "Report date");
        long overdueItems = transactions.stream()
                .filter(transaction -> !transaction.isReturned()
                        && transaction.getDueDate().isBefore(asOfDate))
                .count();
        showReportSummary(overdueItems);
    }

    private void showReportSummary(long overdueItems) {
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
        System.out.println("Overdue items: " + overdueItems);
        System.out.println("Total users: " + users.size());
        System.out.println("Total fines assessed: Rs " + formatAmount(transactions.stream()
            .mapToDouble(Transaction::getFine).sum()));
        System.out.println("Total fines paid: Rs " + formatAmount(transactions.stream()
            .mapToDouble(Transaction::getPaidFine).sum()));
        System.out.println("Outstanding fines: Rs " + formatAmount(transactions.stream()
            .mapToDouble(Transaction::getOutstandingFine).sum()));
        System.out.println("Active reservations: " + items.stream()
            .mapToInt(item -> item.getReservationQueue().size()).sum());
        System.out.println("Most borrowed item: " + (mostBorrowed != null ? mostBorrowed.getTitle() + " (" + mostBorrowed.getBorrowCount() + " times)" : "None"));
    }

    private LocalDate legacyDayToDate(int day) {
        return LocalDate.of(1970, 1, 1).plusDays(day - 1L);
    }
}

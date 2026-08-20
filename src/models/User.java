package models;

import utils.InputValidator;

import java.util.ArrayList;

/**
 * Represents a library user and tracks borrowed items.
 */
public class User extends Person {
    private final ArrayList<LibraryItem> borrowedItems;
    private String email;
    private int maxBorrowLimit = 3;

    public User(String id, String name) {
        this(id, name, "", 3);
    }

    public User(String id, String name, String email) {
        this(id, name, email, 3);
    }

    public User(String id, String name, String email, int maxBorrowLimit) {
        super(id, name);
        this.email = InputValidator.validateOptionalEmail(email);
        this.maxBorrowLimit = Math.max(1, maxBorrowLimit);
        this.borrowedItems = new ArrayList<>();
    }

    /**
     * Adds an item to the user's borrowed list.
     *
     * @param item the item being borrowed
     * @return true when the item was successfully borrowed, false when it was already borrowed
     */
    public boolean borrowItem(LibraryItem item) {
        if (item == null) {
            return false;
        }
        if (borrowedItems.contains(item)) {
            return false;
        }
        borrowedItems.add(item);
        return true;
    }

    /**
     * Removes the returned item from the user's list.
     *
     * @param item the item being returned
     * @return true when the item was present and removed
     */
    public boolean returnBorrowedItem(LibraryItem item) {
        return item != null && borrowedItems.remove(item);
    }

    public ArrayList<LibraryItem> getBorrowedItems() {
        return borrowedItems;
    }

    public boolean canBorrow() {
        return borrowedItems.size() < maxBorrowLimit;
    }

    public String getEmail() {
        return email;
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void displayInfo() {
        System.out.println("User ID: " + id
                + " | Name: " + name
                + " | Email: " + (email.isEmpty() ? "Not provided" : email)
                + " | Max Borrow Limit: " + maxBorrowLimit
                + " | Borrowed Items: " + borrowedItems.size());

        if (!borrowedItems.isEmpty()) {
            System.out.print("  Borrowed item IDs: ");
            for (int i = 0; i < borrowedItems.size(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(borrowedItems.get(i).getItemID());
            }
            System.out.println();
        }
    }

    public String toCsv() {
        return String.join(",",
                escapeCsv(id),
                escapeCsv(name),
                escapeCsv(email),
                String.valueOf(maxBorrowLimit));
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + email + ")";
    }
}

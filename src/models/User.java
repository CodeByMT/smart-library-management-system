package models;

import java.util.ArrayList;

public class User extends Person {
    private final ArrayList<LibraryItem> borrowedItems;

    public User(String id, String name) {
        super(id, name);
        this.borrowedItems = new ArrayList<>();
    }

    public void borrowItem(LibraryItem item) {
        if (!borrowedItems.contains(item)) {
            borrowedItems.add(item);
        }
    }

    public void returnBorrowedItem(LibraryItem item) {
        borrowedItems.remove(item);
    }

    public ArrayList<LibraryItem> getBorrowedItems() {
        return borrowedItems;
    }

    public String toCsv() {
        return id + "," + name;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

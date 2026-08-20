package models;

import interfaces.Borrowable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Base library item for books, e-books, and journals.
 *
 * <p>
 * This class supports the common fields and behaviors used by all
 * item types, including availability status, reservation queue handling,
 * and serialization to CSV format.</p>
 */
public abstract class LibraryItem implements Borrowable {
    protected String itemID;
    protected String title;
    protected String author;
    protected boolean available;
    protected Queue<String> reservationQueue;
    protected int borrowCount;

    public LibraryItem(String itemID, String title, String author, boolean available) {
        this.itemID = itemID;
        this.title = title;
        this.author = author == null ? "" : author;
        this.available = available;
        this.borrowCount = 0;
        this.reservationQueue = new LinkedList<>();
    }

    public abstract double calculateFine(int daysLate);

    public abstract String getType();

    public abstract void displayInfo();

    @Override
    public void issueItem(User user) {
        this.available = false;
        borrowCount++;
    }

    @Override
    public void returnItem(User user) {
        this.available = true;
    }

    public boolean reserveItem(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        if (reservationQueue.contains(userId)) {
            return false;
        }
        return reservationQueue.offer(userId);
    }

    public boolean restoreReservationAtFront(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        if (reservationQueue.contains(userId)) {
            return false;
        }
        if (reservationQueue instanceof LinkedList) {
            ((LinkedList<String>) reservationQueue).addFirst(userId);
            return true;
        }
        return reservationQueue.offer(userId);
    }

    public String getAuthor() {
        return author;
    }

    public String peekReservation() {
        return reservationQueue.peek();
    }

    public String pollReservation() {
        return reservationQueue.poll();
    }

    public boolean removeReservation(String userId) {
        return reservationQueue.remove(userId);
    }

    public boolean hasReservation() {
        return !reservationQueue.isEmpty();
    }

    public String getReservationList() {
        if (reservationQueue.isEmpty()) {
            return "None";
        }

        StringBuilder builder = new StringBuilder();
        for (String userId : reservationQueue) {
            if (builder.length() > 0) {
                builder.append(" -> ");
            }
            builder.append(userId);
        }
        return builder.toString();
    }

    public String getItemID() {
        return itemID;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void incrementBorrowCount() {
        this.borrowCount++;
    }

    public void decrementBorrowCount() {
        if (this.borrowCount > 0) {
            this.borrowCount--;
        }
    }

    public void resetBorrowCount() {
        this.borrowCount = 0;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Serializes this library item to a CSV record.
     *
     * @return a comma-separated string representing this item
     */
    public String toCsv() {
        return String.join(",",
                escapeCsv(itemID),
                escapeCsv(title),
                escapeCsv(author),
                escapeCsv(getType()),
                String.valueOf(available));
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

    /**
     * Returns a readable representation of the library item.
     *
     * @return text containing the ID, title, author, type, and availability
     */
    @Override
    public String toString() {
        return itemID + " - " + title + " by " + (author.isEmpty() ? "Unknown" : author) +
               " (" + getType() + ", " + (available ? "Available" : "Issued") + ")";
    }
}

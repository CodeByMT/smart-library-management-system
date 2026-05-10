package models;

import interfaces.Loanable;

import java.util.LinkedList;
import java.util.Queue;

public abstract class LibraryItem implements Loanable {
    protected String itemID;
    protected String title;
    protected boolean available;
    protected Queue<String> reservationQueue;
    protected int borrowCount;

    public LibraryItem(String itemID, String title, boolean available) {
        this.itemID = itemID;
        this.title = title;
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
        if (reservationQueue.contains(userId)) {
            return false;
        }
        return reservationQueue.offer(userId);
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

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String toCsv() {
        return itemID + "," + title + "," + getType() + "," + available;
    }

    @Override
    public String toString() {
        return itemID + " | " + title + " | " + getType() + " | " + (available ? "Available" : "Issued") + " | Reservations: " + getReservationList();
    }
}

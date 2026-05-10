package models;

public class Book extends LibraryItem {

    public Book(String itemID, String title, boolean available) {
        super(itemID, title, available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return daysLate * 50;
    }

    @Override
    public String getType() {
        return "Book";
    }

    @Override
    public void displayInfo() {
        System.out.println("[BOOK] " + toString());
    }
}

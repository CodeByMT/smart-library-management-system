package models;

public class EBook extends LibraryItem {

    public EBook(String itemID, String title, boolean available) {
        super(itemID, title, available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return daysLate * 30;
    }

    @Override
    public String getType() {
        return "EBook";
    }

    @Override
    public void displayInfo() {
        System.out.println("[EBOOK] " + toString());
    }
}

package models;

public class Journal extends LibraryItem {

    public Journal(String itemID, String title, boolean available) {
        super(itemID, title, available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return daysLate * 80;
    }

    @Override
    public String getType() {
        return "Journal";
    }

    @Override
    public void displayInfo() {
        System.out.println("[JOURNAL] " + toString());
    }
}

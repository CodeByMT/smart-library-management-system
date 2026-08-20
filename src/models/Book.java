package models;

import interfaces.FineCalculationStrategy;

/**
 * Represents a physical book in the library.
 * 
 * Books incur fines for late returns: Rs 50 per day after 2-day grace period.
 * This encourages timely return of physical items while allowing minor delays.
 */
public class Book extends LibraryItem {
    /**
     * Fine strategy: Rs 50 per day after 2-day grace period
     */
    private static final FineCalculationStrategy FINE_STRATEGY = new FineCalculationStrategy() {
        @Override
        public double calculateFine(int daysLate) {
            if (daysLate <= 2) {
                return 0;  // 2-day grace period
            }
            return (daysLate - 2) * 50;  // Rs 50 per day after grace period
        }
        
        @Override
        public String getDescription() {
            return "Rs 50 per day after 2-day grace period";
        }
    };
    
    public Book(String itemID, String title, String author, boolean available) {
        super(itemID, title, author, available);
    }

    public Book(String itemID, String title, boolean available) {
        this(itemID, title, "", available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return FINE_STRATEGY.calculateFine(daysLate);
    }

    @Override
    public String getType() {
        return "Book";
    }

    @Override
    public void displayInfo() {
        System.out.println(toString());
    }
}

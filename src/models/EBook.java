package models;

import interfaces.FineCalculationStrategy;

/**
 * Represents an electronic book (e-book) in the library.
 * 
 * E-books have no overdue fines since they can be instantly revoked
 * and don't have the same physical constraints as printed books.
 */
public class EBook extends LibraryItem {
    /**
     * Fine strategy: No fines for e-books
     */
    private static final FineCalculationStrategy FINE_STRATEGY = new FineCalculationStrategy() {
        @Override
        public double calculateFine(int daysLate) {
            return 0;  // No fines for e-books
        }
        
        @Override
        public String getDescription() {
            return "No fines (instant access control)";
        }
    };
    
    public EBook(String itemID, String title, boolean available) {
        this(itemID, title, "", available);
    }

    public EBook(String itemID, String title, String author, boolean available) {
        super(itemID, title, author, available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return FINE_STRATEGY.calculateFine(daysLate);
    }

    @Override
    public String getType() {
        return "EBook";
    }

    @Override
    public void displayInfo() {
        System.out.println(toString());
    }
}

package models;

import interfaces.FineCalculationStrategy;

/**
 * Represents a journal article or publication in the library.
 * 
 * Journals incur fines for late returns: Rs 80 per day after 2-day grace period.
 * Higher rate than books reflects the value and demand for academic journals.
 */
public class Journal extends LibraryItem {
    /**
     * Fine strategy: Rs 80 per day after 2-day grace period
     */
    private static final FineCalculationStrategy FINE_STRATEGY = new FineCalculationStrategy() {
        @Override
        public double calculateFine(int daysLate) {
            if (daysLate <= 2) {
                return 0;  // 2-day grace period
            }
            return (daysLate - 2) * 80;  // Rs 80 per day after grace period
        }
        
        @Override
        public String getDescription() {
            return "Rs 80 per day after 2-day grace period";
        }
    };
    
    public Journal(String itemID, String title, String author, boolean available) {
        super(itemID, title, author, available);
    }

    public Journal(String itemID, String title, boolean available) {
        this(itemID, title, "", available);
    }

    @Override
    public double calculateFine(int daysLate) {
        return FINE_STRATEGY.calculateFine(daysLate);
    }

    @Override
    public String getType() {
        return "Journal";
    }

    @Override
    public void displayInfo() {
        System.out.println(toString());
    }
}

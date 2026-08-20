package interfaces;

/**
 * Strategy interface for calculating fines based on item type and days late.
 * Enables different fine calculation rules for different item types without
 * hardcoding the logic in each item class.
 * 
 * This implements the Strategy pattern - each item type can have its own
 * fine calculation strategy.
 */
public interface FineCalculationStrategy {
    
    /**
     * Calculates the fine amount for late return.
     * 
     * @param daysLate the number of days the item was overdue
     * @return the fine amount in rupees/currency units
     */
    double calculateFine(int daysLate);
    
    /**
     * Gets a description of this fine calculation strategy.
     * @return description of the fine calculation rules
     */
    String getDescription();
}

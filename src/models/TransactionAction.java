package models;

/**
 * Enumeration representing different types of library transactions.
 * Replaces magic strings like "ISSUE", "RETURN", "RESERVE" with type-safe enum.
 */
public enum TransactionAction {
    ISSUE("Item issued to user", "Reverse: return item to available state"),
    RETURN("Item returned by user", "Reverse: mark item as borrowed again"),
    RESERVE("Item reserved by user", "Reverse: remove user from reservation queue");
    
    private final String description;
    private final String undoDescription;
    
    TransactionAction(String description, String undoDescription) {
        this.description = description;
        this.undoDescription = undoDescription;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUndoDescription() {
        return undoDescription;
    }
}

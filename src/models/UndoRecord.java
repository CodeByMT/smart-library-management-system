package models;

/**
 * Strongly-typed record for undo operations.
 * Replaces the previous approach of passing loose parameters to UndoRecord.
 * 
 * This class encapsulates all information needed to reverse a transaction:
 * - The action type (ISSUE, RETURN, RESERVE)
 * - The transaction details
 * - The affected item and user
 * - Whether a reservation was removed during the action
 */
public class UndoRecord {
    private final TransactionAction action;
    private final Transaction transaction;
    private final String itemId;
    private final String userId;
    private final boolean reservationRemoved;
    
    /**
     * Creates an undo record for a transaction.
     * 
     * @param action the type of transaction being undone
     * @param transaction the transaction details (may be null for RESERVE actions)
     * @param itemId the ID of the affected library item
     * @param userId the ID of the affected user
     * @param reservationRemoved whether a reservation was removed during the action
     */
    public UndoRecord(TransactionAction action, Transaction transaction, 
                     String itemId, String userId, boolean reservationRemoved) {
        this.action = action;
        this.transaction = transaction;
        this.itemId = itemId;
        this.userId = userId;
        this.reservationRemoved = reservationRemoved;
    }
    
    public TransactionAction getAction() {
        return action;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public boolean isReservationRemoved() {
        return reservationRemoved;
    }
    
    @Override
    public String toString() {
        return String.format("UndoRecord{action=%s, item=%s, user=%s}", 
                           action.name(), itemId, userId);
    }
}

package models;

public class Transaction {
    private String transactionID;
    private String userID;
    private String itemID;
    private int issueDay;
    private int dueDay;
    private int returnDay;
    private double fine;

    public Transaction(String transactionID, String userID, String itemID, int issueDay, int dueDay, int returnDay, double fine) {
        this.transactionID = transactionID;
        this.userID = userID;
        this.itemID = itemID;
        this.issueDay = issueDay;
        this.dueDay = dueDay;
        this.returnDay = returnDay;
        this.fine = fine;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getUserID() {
        return userID;
    }

    public String getItemID() {
        return itemID;
    }

    public int getIssueDay() {
        return issueDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public int getReturnDay() {
        return returnDay;
    }

    public double getFine() {
        return fine;
    }

    public void setReturnDay(int returnDay) {
        this.returnDay = returnDay;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public boolean isReturned() {
        return returnDay > 0;
    }

    public String toCsv() {
        return transactionID + "," + userID + "," + itemID + "," + issueDay + "," + dueDay + "," + returnDay + ","
                + fine;
    }

    @Override
    public String toString() {
        return "Transaction(" + transactionID + ", User=" + userID + ", Item=" + itemID + ", Issue=" + issueDay
                + ", Due=" + dueDay + ", Return=" + returnDay + ", Fine=" + fine + ")";
    }

}

    
                
                

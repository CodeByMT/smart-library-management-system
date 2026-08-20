package models;

/**
 * Represents an issue or return transaction for a library item.
 */
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
        return String.join(",",
                escapeCsv(transactionID),
                escapeCsv(userID),
                escapeCsv(itemID),
                String.valueOf(issueDay),
                String.valueOf(dueDay),
                String.valueOf(returnDay),
                String.valueOf(fine));
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public String toString() {
        return "Transaction(" + transactionID + ", User=" + userID + ", Item=" + itemID + ", Issue=" + issueDay
                + ", Due=" + dueDay + ", Return=" + returnDay + ", Fine=" + fine + ")";
    }

}
    
                
                

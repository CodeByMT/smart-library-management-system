package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents an issue or return transaction for a library item.
 */
public class Transaction {
    private static final LocalDate LEGACY_EPOCH = LocalDate.of(1970, 1, 1);

    private final String transactionID;
    private final String userID;
    private final String itemID;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;
    private double paidFine;
    private LocalDate paymentDate;
    private final boolean legacyNumericDates;

    public Transaction(String transactionID, String userID, String itemID, int issueDay, int dueDay, int returnDay, double fine) {
        this(transactionID, userID, itemID,
                legacyDayToDate(issueDay),
                legacyDayToDate(dueDay),
                returnDay > 0 ? legacyDayToDate(returnDay) : null,
                fine, 0.0, null,
                true);
    }

    public Transaction(String transactionID, String userID, String itemID,
                       int issueDay, int dueDay, int returnDay, double fine,
                       double paidFine, LocalDate paymentDate) {
        this(transactionID, userID, itemID,
                legacyDayToDate(issueDay),
                legacyDayToDate(dueDay),
                returnDay > 0 ? legacyDayToDate(returnDay) : null,
                fine, paidFine, paymentDate, true);
    }

    public Transaction(String transactionID, String userID, String itemID,
                       LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, double fine) {
        this(transactionID, userID, itemID, issueDate, dueDate, returnDate, fine, 0.0, null, false);
    }

    public Transaction(String transactionID, String userID, String itemID,
                       LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                       double fine, double paidFine, LocalDate paymentDate) {
        this(transactionID, userID, itemID, issueDate, dueDate, returnDate,
                fine, paidFine, paymentDate, false);
    }

    private Transaction(String transactionID, String userID, String itemID,
                        LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                        double fine, double paidFine, LocalDate paymentDate,
                        boolean legacyNumericDates) {
        this.transactionID = transactionID;
        this.userID = userID;
        this.itemID = itemID;
        if (issueDate == null || dueDate == null || dueDate.isBefore(issueDate)
                || (returnDate != null && returnDate.isBefore(issueDate))) {
            throw new IllegalArgumentException("Transaction dates are invalid.");
        }
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        if (paidFine < 0 || paidFine > fine) {
            throw new IllegalArgumentException("Paid fine must be between zero and the assessed fine.");
        }
        if (paidFine > 0 && paymentDate == null) {
            throw new IllegalArgumentException("A payment date is required for a payment.");
        }
        this.paidFine = paidFine;
        this.paymentDate = paymentDate;
        this.legacyNumericDates = legacyNumericDates;
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
        return dateToLegacyDay(issueDate);
    }

    public int getDueDay() {
        return dateToLegacyDay(dueDate);
    }

    public int getReturnDay() {
        return returnDate == null ? 0 : dateToLegacyDay(returnDate);
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public double getFine() {
        return fine;
    }

    public double getPaidFine() {
        return paidFine;
    }

    public double getOutstandingFine() {
        return fine - paidFine;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setReturnDay(int returnDay) {
        this.returnDate = returnDay > 0 ? legacyDayToDate(returnDay) : null;
    }

    public void setReturnDate(LocalDate returnDate) {
        if (returnDate != null && returnDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Return date cannot be before issue date.");
        }
        this.returnDate = returnDate;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public void recordPayment(double amount, LocalDate paymentDate) {
        if (amount <= 0 || paymentDate == null) {
            throw new IllegalArgumentException("Payment amount must be positive and payment date is required.");
        }
        if (amount > getOutstandingFine()) {
            throw new IllegalArgumentException("Payment cannot exceed the outstanding fine.");
        }
        this.paidFine += amount;
        this.paymentDate = paymentDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public String toCsv() {
        return String.join(",",
                escapeCsv(transactionID),
                escapeCsv(userID),
                escapeCsv(itemID),
                formatDate(issueDate),
                formatDate(dueDate),
                returnDate == null ? (legacyNumericDates ? "0" : "") : formatDate(returnDate),
                String.valueOf(fine),
                String.valueOf(paidFine),
                paymentDate == null ? "" : formatDate(paymentDate));
    }

    private String formatDate(LocalDate date) {
        if (legacyNumericDates) {
            return String.valueOf(dateToLegacyDay(date));
        }
        return date.toString();
    }

    private static LocalDate legacyDayToDate(int day) {
        if (day <= 0) {
            return null;
        }
        return LEGACY_EPOCH.plusDays(day - 1L);
    }

    private static int dateToLegacyDay(LocalDate date) {
        return Math.toIntExact(ChronoUnit.DAYS.between(LEGACY_EPOCH, date) + 1);
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
        return "Transaction(" + transactionID + ", User=" + userID + ", Item=" + itemID + ", Issue=" + issueDate
            + ", Due=" + dueDate + ", Return=" + returnDate + ", Fine=" + fine + ")";
    }

}
    
                
                

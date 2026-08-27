package utils;

import java.time.LocalDate;

/**
 * Centralized validation utility for library system inputs.
 * Provides reusable validation methods to ensure data integrity.
 */
public class InputValidator {
    
    /**
     * Validates that a string is not null or empty.
     * @param input the input string to validate
     * @param fieldName the name of the field for error messages
     * @return the trimmed input if valid
     * @throws IllegalArgumentException if input is null or empty
     */
    public static String validateNonEmpty(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return input.trim();
    }

    /**
     * Validates an optional email value.
     * Empty values are permitted; non-empty values must be valid email addresses.
     *
     * @param email the email to validate
     * @return the trimmed email, or an empty string if it is blank
     * @throws IllegalArgumentException if a non-empty value is not a valid email
     */
    public static String validateOptionalEmail(String email) {
        if (email == null) {
            return "";
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        if (!trimmed.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + trimmed);
        }

        return trimmed;
    }
    
    /**
     * Validates that an integer is positive (> 0).
     * @param value the integer to validate
     * @param fieldName the name of the field for error messages
     * @return the value if valid
     * @throws IllegalArgumentException if value is not positive
     */
    public static int validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive integer.");
        }
        return value;
    }
    
    /**
     * Validates that one day is not before another.
     * @param laterDay the day that should be >= earlierDay
     * @param earlierDay the day to compare against
     * @param laterFieldName name of the later field (e.g., "Return day")
     * @param earlierFieldName name of the earlier field (e.g., "Issue day")
     * @throws IllegalArgumentException if laterDay < earlierDay
     */
    public static void validateDaySequence(int laterDay, int earlierDay, 
                                          String laterFieldName, String earlierFieldName) {
        if (laterDay < earlierDay) {
            throw new IllegalArgumentException(
                laterFieldName + " (" + laterDay + ") cannot be before " + 
                earlierFieldName + " (" + earlierDay + ").");
        }
    }

    public static LocalDate validateDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }
        return date;
    }

    public static void validateDateSequence(LocalDate laterDate, LocalDate earlierDate,
                                            String laterFieldName, String earlierFieldName) {
        validateDate(laterDate, laterFieldName);
        validateDate(earlierDate, earlierFieldName);
        if (laterDate.isBefore(earlierDate)) {
            throw new IllegalArgumentException(
                    laterFieldName + " (" + laterDate + ") cannot be before "
                            + earlierFieldName + " (" + earlierDate + ").");
        }
    }
}

package exceptions;

public class OverdueException extends Exception {
    public OverdueException() {
        super("The item is overdue.");
    }

    public OverdueException(String message) {
        super(message);
    }

    public OverdueException(String message, Throwable cause) {
        super(message, cause);
    }

    public OverdueException(Throwable cause) {
        super(cause);
    }
}
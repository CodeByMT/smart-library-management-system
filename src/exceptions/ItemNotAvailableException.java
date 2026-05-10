package exceptions;

public class ItemNotAvailableException extends Exception {
    public ItemNotAvailableException() {
        super("The requested item is not available.");
    }

    public ItemNotAvailableException(String message) {
        super(message);
    }

    public ItemNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemNotAvailableException(Throwable cause) {
        super(cause);
    }
}

package exceptions;

public class InvalidUserException extends Exception {
    public InvalidUserException() {
        super("The user ID is invalid or not registered.");
    }

    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUserException(Throwable cause) {
        super(cause);
    }
}

package igirepay.igire_capstoneproject.lab1.exception;

public class DuplicateTransactionException extends Exception {
    // Default constructor
    public DuplicateTransactionException() {
        super("Duplicate transaction detected. This reference ID has already been processed.");
    }
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
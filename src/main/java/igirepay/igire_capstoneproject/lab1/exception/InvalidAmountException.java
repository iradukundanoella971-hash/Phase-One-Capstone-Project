package igirepay.igire_capstoneproject.lab1.exception;

public class InvalidAmountException extends Exception {
    // Default constructor
    public InvalidAmountException() {
        super("Invalid amount provided.");
    }

    // Constructor with custom message
    public InvalidAmountException(String message) {
        super(message);
    }
}
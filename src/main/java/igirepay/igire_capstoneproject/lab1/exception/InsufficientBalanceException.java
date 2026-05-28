package igirepay.igire_capstoneproject.lab1.exception;

public class InsufficientBalanceException extends Exception {
    // Default constructor
    public InsufficientBalanceException() {
        super("Insufficient balance in account.");
    }

    // Constructor with custom message
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
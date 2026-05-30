package igirepay.igire_capstoneproject.lab1.exception;

public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException() {
        super("Insufficient balance in account.");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
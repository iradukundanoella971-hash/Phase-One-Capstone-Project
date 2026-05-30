package igirepay.igire_capstoneproject.lab1.exception;

public class InvalidAmountException extends Exception {
    public InvalidAmountException() {
        super("Invalid amount provided.");
    }
    public InvalidAmountException(String message) {
        super(message);
    }
}
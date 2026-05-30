package igirepay.igire_capstoneproject.lab1.util;

public class ReferenceIdGenerator {
    private static int refCounter = 0;
    private static int txnCounter = 0;
    public static String generateReferenceId() {
        refCounter++;
        return String.format("REF-%04d", refCounter);
    }
    public static String generateTransactionId() {
        txnCounter++;
        return String.format("TXN-%04d", txnCounter);
    }
    public static void resetCounters() {
        refCounter = 0;
        txnCounter = 0;
    }
}
package igirepay.igire_capstoneproject.lab1.util;

public class ReferenceIdGenerator {
    // Static counters
    private static int refCounter = 0;   // For reference IDs (REF-XXXX)
    private static int txnCounter = 0;   // For transaction IDs (TXN-XXXX)

    // Generate next Reference ID
    public static String generateReferenceId() {
        refCounter++;
        return String.format("REF-%04d", refCounter);
    }
    public static String generateTransactionId() {
        txnCounter++;
        return String.format("TXN-%04d", txnCounter);
    }

    // Reset counters
    public static void resetCounters() {
        refCounter = 0;
        txnCounter = 0;
    }
}
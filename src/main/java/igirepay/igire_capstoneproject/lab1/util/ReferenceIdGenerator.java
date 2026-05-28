package igirepay.igire_capstoneproject.lab1.utils;

public class ReferenceIdGenerator {
    // Static counters (shared across all calls)
    private static int refCounter = 0;   // For reference IDs (REF-XXXX)
    private static int txnCounter = 0;   // For transaction IDs (TXN-XXXX)

    // Generate next Reference ID (format: REF-0001, REF-0002, ...)
    public static String generateReferenceId() {
        refCounter++;
        return String.format("REF-%04d", refCounter);
    }

    // Generate next Transaction ID (format: TXN-0001, TXN-0002, ...)
    public static String generateTransactionId() {
        txnCounter++;
        return String.format("TXN-%04d", txnCounter);
    }

    // Reset counters (useful for testing, not required for main flow)
    public static void resetCounters() {
        refCounter = 0;
        txnCounter = 0;
    }
}
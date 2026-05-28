// ===== File: FeeCalculator.java =====
package igirepay.igire_capstoneproject.lab1.util;

public class FeeCalculator {
    public static double calculateTransferFee(double amount) {
        if (amount <= 1000) {
            return 20.0;
        } else if (amount <= 10000) {
            return 100.0;
        } else if (amount <= 300000) {
            return 250.0;
        } else {
            // For amounts above 300,000 RWF, a fixed 500 fee (custom rule, but spec stops at 300k)
            return 500.0;
        }
    }
}
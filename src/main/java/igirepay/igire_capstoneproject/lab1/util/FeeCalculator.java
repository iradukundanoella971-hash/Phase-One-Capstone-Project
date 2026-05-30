
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
            return 500.0;
        }
    }
}
// ===== File: IdGenerator.java =====
package igirepay.igire_capstoneproject.lab1.util;

import java.util.UUID;

public class IdGenerator {
    public static String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    public static String generateCustomerId() {
        return "CUST-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    public static String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
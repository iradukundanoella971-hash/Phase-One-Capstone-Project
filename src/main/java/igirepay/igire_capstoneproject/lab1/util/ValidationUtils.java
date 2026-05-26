// ===== File: ValidationUtils.java =====
package igirepay.igire_capstoneproject.lab1.util;

public class ValidationUtils {
    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{5}");
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10,12}");
    }
}
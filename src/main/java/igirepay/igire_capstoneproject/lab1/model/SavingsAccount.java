// ===== File: SavingsAccount.java =====
package igirepay.igire_capstoneproject.lab1.model;

public class SavingsAccount extends Account {
    private static final double WITHDRAWAL_FEE = 100.0;

    public SavingsAccount(String accountNumber, String pin, String accountHolderName) {
        super(accountNumber, pin, accountHolderName);
    }

    @Override
    public void processTransaction(Transaction transaction) {
        System.out.println("[Savings] Applying special rules for transaction: " + transaction.getReferenceId());
    }

    @Override
    public boolean withdraw(double amount) {
        double totalDeduction = amount + WITHDRAWAL_FEE;
        if (amount > 0 && getBalance() >= totalDeduction) {
            setBalance(getBalance() - amount);
            setBalance(getBalance() - WITHDRAWAL_FEE);
            return true;
        }
        return false;
    }
}

package igirepay.igire_capstoneproject.lab1.model;

public class WalletAccount extends Account {
    public WalletAccount(String accountNumber, String pin, String accountHolderName) {
        super(accountNumber, pin, accountHolderName);
    }

    @Override
    public void processTransaction(Transaction transaction) {
        System.out.println("[Wallet] Processing transaction: " + transaction.getReferenceId());
    }

    @Override
    public boolean withdraw(double amount) {
        return super.withdraw(amount);
    }
}
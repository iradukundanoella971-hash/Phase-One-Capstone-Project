// ===== File: TransactionService.java =====
package igirepay.igire_capstoneproject.lab1.service;

import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab1.util.IdGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionService {
    public String generateUniqueReferenceId(Set<String> processedRefIds) {
        String refId;
        do {
            refId = IdGenerator.generateTransactionId();
        } while (processedRefIds.contains(refId));
        return refId;
    }

    public boolean isDuplicateReference(String referenceId, Set<String> processedRefIds) {
        return processedRefIds.contains(referenceId);
    }

    public boolean isDuplicateReference(String referenceId) {
        // Overload for convenience, requires set access from service state
        // We'll pass set explicitly in calls
        return false; // dummy, actual call uses set param
    }

    public void recordTransaction(Transaction transaction, Set<String> processedRefIds, List<Transaction> transactionHistory) {
        processedRefIds.add(transaction.getReferenceId());
        transactionHistory.add(transaction);
    }

    public void logFailedTransaction(String referenceId, String reason, Map<String, String> failedTxLogs) {
        failedTxLogs.put(referenceId, reason);
    }

    public void displayTransactionHistory(List<Transaction> transactionHistory) {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        for (Transaction tx : transactionHistory) {
            System.out.println(tx);
        }
    }

    public void displayFailedTransactions(Map<String, String> failedTxLogs) {
        if (failedTxLogs.isEmpty()) {
            System.out.println("No failed transactions.");
            return;
        }
        for (Map.Entry<String, String> entry : failedTxLogs.entrySet()) {
            System.out.println("Ref: " + entry.getKey() + " | Reason: " + entry.getValue());
        }
    }
}
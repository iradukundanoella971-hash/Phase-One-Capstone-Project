
package igirepay.igire_capstoneproject.lab1.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Transaction;

public class TransactionService {
    public UUID generateUniqueReferenceId(Set<UUID> processedRefIds) {
        UUID refId;
        do {
            refId = UUID.randomUUID();
        } while (processedRefIds.contains(refId));
        return refId;
    }

    public boolean isDuplicateReference(UUID referenceId, Set<UUID> processedRefIds) {
        return processedRefIds.contains(referenceId);
    }

    public void recordTransaction(Transaction transaction, Set<UUID> processedRefIds, List<Transaction> transactionHistory) {
        processedRefIds.add(transaction.getReferenceId());
        transactionHistory.add(transaction);
    }

    public void logFailedTransaction(UUID referenceId, String reason, Map<UUID, String> failedTxLogs) {
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

    public void displayFailedTransactions(Map<UUID, String> failedTxLogs) {
        if (failedTxLogs.isEmpty()) {
            System.out.println("No failed transactions.");
            return;
        }
        for (Map.Entry<UUID, String> entry : failedTxLogs.entrySet()) {
            System.out.println("Ref: " + entry.getKey() + " | Reason: " + entry.getValue());
        }
    }
}
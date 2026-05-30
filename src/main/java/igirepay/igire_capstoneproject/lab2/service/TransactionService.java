package igirepay.igire_capstoneproject.lab2.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab2.dao.ProcessedRequestDAO;
import igirepay.igire_capstoneproject.lab2.dao.TransactionDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.ProcessedRequestDAOImpl;
import igirepay.igire_capstoneproject.lab2.dao.impl.TransactionDAOImpl;

public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final ProcessedRequestDAO processedRequestDAO;

    public TransactionService() {
        this(new TransactionDAOImpl(), new ProcessedRequestDAOImpl());
    }

    public TransactionService(TransactionDAO transactionDAO, ProcessedRequestDAO processedRequestDAO) {
        this.transactionDAO = transactionDAO;
        this.processedRequestDAO = processedRequestDAO;
    }

    public UUID generateReferenceId() {
        return UUID.randomUUID();
    }

    public boolean isDuplicateReference(UUID referenceId) {
        return processedRequestDAO.existsByReferenceId(referenceId);
    }

    public void recordAndMarkProcessed(Transaction tx) {
        transactionDAO.create(tx);
        processedRequestDAO.create(tx.getReferenceId(), new Timestamp(System.currentTimeMillis()));
    }

    public List<Transaction> getTransactionHistoryForAccountId(UUID accountId) {
        return transactionDAO.findByAccountId(accountId);
    }

    public void recordFailure(Transaction tx, String reason) {
        tx.setFailureReason(reason);
        tx.setStatus("FAILED");
        transactionDAO.create(tx);
    }
}


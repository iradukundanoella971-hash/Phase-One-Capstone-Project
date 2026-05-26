package igirepay.igire_capstoneproject.lab2.dao;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Transaction;

public interface TransactionDAO {
    void create(Transaction transaction);
    Transaction findById(UUID id);
    List<Transaction> findAll();
    void update(Transaction transaction);
    void delete(UUID id);

    List<Transaction> findByAccountId(UUID accountId);
}


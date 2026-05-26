package igirepay.igire_capstoneproject.lab2.dao;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Account;

public interface AccountDAO {
    void create(Account account, UUID customerId, String accountType);

    Account findById(UUID id);

    List<Account> findAll();

    void update(Account account);

    void delete(UUID id);

    Account findByAccountNumber(String accountNumber);
}


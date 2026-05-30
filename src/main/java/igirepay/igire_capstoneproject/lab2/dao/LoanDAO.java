package igirepay.igire_capstoneproject.lab2.dao;

import igirepay.igire_capstoneproject.lab2.model.Loan;

import java.util.List;
import java.util.UUID;

public interface LoanDAO {
    void create(Loan loan);
    Loan findById(UUID id);
    Loan findOutstandingByUserId(UUID userId);
    List<Loan> findByUserId(UUID userId);
    List<Loan> findAll();
    void update(Loan loan);
}

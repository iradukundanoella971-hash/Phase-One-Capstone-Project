package igirepay.igire_capstoneproject.lab2.service;

import igirepay.igire_capstoneproject.lab2.dao.LoanDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.LoanDAOImpl;
import igirepay.igire_capstoneproject.lab2.model.Loan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LoanService {

    private final LoanDAO loanDAO;

    public LoanService() {
        this(new LoanDAOImpl());
    }

    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    public Loan findOutstandingLoan(UUID userId) {
        if (userId == null) {
            return null;
        }
        Loan loan = loanDAO.findOutstandingByUserId(userId);
        if (loan == null) {
            return null;
        }
        if ("ACTIVE".equalsIgnoreCase(loan.getStatus()) && loan.getDueDate() != null
                && loan.getDueDate().isBefore(LocalDate.now())) {
            loan.setStatus("OVERDUE");
            loanDAO.update(loan);
        }
        return loan;
    }

    public Loan createLoan(UUID userId, double amount) {
        LocalDate issueDate = LocalDate.now();
        Loan loan = new Loan(
                UUID.randomUUID(),
                userId,
                amount,
                issueDate,
                issueDate.plusDays(30),
                "UNPAID",
                "ACTIVE"
        );
        loanDAO.create(loan);
        return loan;
    }

    public void markPaid(Loan loan) {
        if (loan == null) {
            return;
        }
        loan.setRepaymentStatus("PAID");
        loan.setStatus("PAID");
        loanDAO.update(loan);
    }

    public List<Loan> listLoansForUser(UUID userId) {
        return loanDAO.findByUserId(userId);
    }
}

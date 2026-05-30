package igirepay.igire_capstoneproject.lab2.model;

import java.time.LocalDate;
import java.util.UUID;

public class Loan {
    private UUID loanId;
    private UUID userId;
    private double loanAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String repaymentStatus;
    private String status;

    public Loan(UUID loanId, UUID userId, double loanAmount, LocalDate issueDate,
                LocalDate dueDate, String repaymentStatus, String status) {
        this.loanId = loanId;
        this.userId = userId;
        this.loanAmount = loanAmount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.repaymentStatus = repaymentStatus;
        this.status = status;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getRepaymentStatus() {
        return repaymentStatus;
    }

    public void setRepaymentStatus(String repaymentStatus) {
        this.repaymentStatus = repaymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(repaymentStatus);
    }
}

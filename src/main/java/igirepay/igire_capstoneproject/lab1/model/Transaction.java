package igirepay.igire_capstoneproject.lab1.model;

import java.time.LocalDateTime;

public class Transaction {
    private String referenceId;
    private double amount;
    private double fee;
    private LocalDateTime timestamp;
    private String type;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private String status;
    private String failureReason;

    public Transaction(String referenceId, double amount, double fee, String type,
                       String sourceAccountNumber, String targetAccountNumber, String status) {
        this.referenceId = referenceId;
        this.amount = amount;
        this.fee = fee;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.status = status;
    }

    // ====== ALL GETTERS AND SETTERS ======
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public void setTargetAccountNumber(String targetAccountNumber) { this.targetAccountNumber = targetAccountNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    @Override
    public String toString() {
        return String.format("Txn[%s] %s: %.2f RWF (fee: %.2f) from %s to %s [%s]",
                referenceId, type, amount, fee, sourceAccountNumber, targetAccountNumber, status);
    }
}
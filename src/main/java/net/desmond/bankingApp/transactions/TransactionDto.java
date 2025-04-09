package net.desmond.bankingApp.transactions;

public class TransactionDto {

    private Long transId;
    private Long senderId;
    private Long receiverId;
    private double amount;
    private String status;
    private String timestamp;

    public TransactionDto(Long transId, Long senderId, Long receiverId, double amount, String status, String timestamp) {
        this.transId = transId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

package net.desmond.bankingApp.transactions;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name="transaction_history")
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transId;

    @Column(name = "sender_Id")
    private Long senderId;

    @Column(name = "receiver_Id")
    private Long receiverId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "status")
    private String status;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "sender_aes_encrypted_key", columnDefinition = "TEXT")
    private String senderAesEncryptedKey;

    public Transaction() {
    }

    public Transaction(Long transId, Long senderId, Long receiverId, String amount, String status, String timestamp, String senderAesEncryptedKey) {
        this.transId = transId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.senderAesEncryptedKey = senderAesEncryptedKey;
    }

    public Transaction(Long senderId, Long receiverId, String amount, String status, String senderAesEncryptedKey) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
        this.timestamp = String.valueOf(LocalDateTime.now());
        this.senderAesEncryptedKey = senderAesEncryptedKey;
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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

    public String getSenderAesEncryptedKey() {
        return senderAesEncryptedKey;
    }

    public void setSenderAesEncryptedKey(String senderAesEncryptedKey) {
        this.senderAesEncryptedKey = senderAesEncryptedKey;
    }
}
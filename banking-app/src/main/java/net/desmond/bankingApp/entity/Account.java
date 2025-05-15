package net.desmond.bankingApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
@Getter
@Setter
@Table(name="accounts")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_balance")
    private String balance;

    @Column(name = "account_holder_address")
    private String accountHolderAddress;

    @Column(name = "account_holder_email_address")
    private String accountHolderEmailAddress;

    @Column(name = "aes_encrypted_key", columnDefinition = "TEXT")
    private String aesEncryptedKey;

    @Column(name = "rsa_public_key", columnDefinition = "TEXT")
    private String rsaPublicKey;

    @Column(name = "role")
    private String role;

    public Account() {}

    public Account(Long accountId, String accountHolderName, String balance, String accountHolderAddress, String accountHolderEmailAddress) {
        this.accountId = accountId;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.accountHolderAddress = accountHolderAddress;
        this.accountHolderEmailAddress = accountHolderEmailAddress;
    }

    public Account(Account account) {
        this.accountId =account.getAccountId();
        this.accountHolderAddress=account.getAccountHolderAddress();
        this.accountHolderName=account.getAccountHolderName();
        this.accountHolderEmailAddress=account.getAccountHolderEmailAddress();
        this.balance=account.getBalance();
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAccountHolderAddress() {
        return accountHolderAddress;
    }

    public void setAccountHolderAddress(String accountHolderAddress) {
        this.accountHolderAddress = accountHolderAddress;
    }

    public String getAccountHolderEmailAddress() {
        return accountHolderEmailAddress;
    }

    public void setAccountHolderEmailAddress(String accountHolderEmailAddress) {
        this.accountHolderEmailAddress = accountHolderEmailAddress;
    }

    public String getAesEncryptedKey() {
        return aesEncryptedKey;
    }

    public void setAesEncryptedKey(String aesEncryptedKey) {
        this.aesEncryptedKey = aesEncryptedKey;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

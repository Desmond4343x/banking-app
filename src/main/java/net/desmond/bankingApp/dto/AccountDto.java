package net.desmond.bankingApp.dto;

import lombok.*;

@Data
@Getter
@Setter
public class AccountDto {

    private Long accountId;
    private String accountHolderName;
    private double balance;
    private String accountHolderAddress;
    private String accountHolderEmailAddress;

    public AccountDto() {
    }

    public AccountDto(Long accountId, String accountHolderName, double balance, String accountHolderAddress, String accountHolderEmailAddress) {
        this.accountId = accountId;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.accountHolderAddress = accountHolderAddress;
        this.accountHolderEmailAddress = accountHolderEmailAddress;
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
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
}

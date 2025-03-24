package net.desmond.bankingApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name="accounts")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private AccountDetails accountDetails; // Name and Balance are grouped here

    @Column(name = "aes_encrypted_key")
    private String aesEncryptedKey;

    @Column(name = "rsa_public_key", columnDefinition = "TEXT")
    private String rsaPublicKey;

    public Account() {}

    public Account(Long id, AccountDetails accountDetails) {
        this.id = id;
        this.accountDetails = accountDetails;
    }

    public Long getId() {
        return id;
    }

    public AccountDetails getAccountDetails() {
        return accountDetails;
    }

    public String getAesEncryptedKey() {
        return aesEncryptedKey;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setAccountDetails(AccountDetails accountDetails) {
        this.accountDetails = accountDetails;
    }

    public void setAesEncryptedKey(String aesEncryptedKey) {
        this.aesEncryptedKey = aesEncryptedKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }
}

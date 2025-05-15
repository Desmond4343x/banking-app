package net.desmond.bankingApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name="accounts_credentials")
@Entity
public class AccountCred {

    @Id
    private Long id;

    @Column(name = "hashed_user_password")
    private String hashedUserPassword;

    @Column(name = "rsa_private_key", columnDefinition = "TEXT")
    private String rsaPrivateKey;

    public AccountCred() {}

    public AccountCred(Long id, String hashedUserPassword, String rsaPrivateKey) {
        this.id = id;
        this.hashedUserPassword = hashedUserPassword;
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getHashedUserPassword() { return hashedUserPassword; }

    public void setHashedUserPassword(String hashedUserPassword) { this.hashedUserPassword = hashedUserPassword; }

    public String getRsaPrivateKey() { return rsaPrivateKey; }

    public void setRsaPrivateKey(String rsaPrivateKey) { this.rsaPrivateKey = rsaPrivateKey; }

}

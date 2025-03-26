package net.desmond.bankingApp.secureVault;

import net.desmond.bankingApp.entity.AccountCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountCredRepository extends JpaRepository<AccountCred,Long> {
    @Query("SELECT a.rsaPrivateKey FROM AccountCred a WHERE a.id = :accountId")
    String findPrivateKeyByAccountId(@Param("accountId") Long accountId);
}

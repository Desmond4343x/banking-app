package net.desmond.bankingApp.secureVault;

import net.desmond.bankingApp.entity.AccountCred;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountCredRepository extends JpaRepository<AccountCred,Long> {

}

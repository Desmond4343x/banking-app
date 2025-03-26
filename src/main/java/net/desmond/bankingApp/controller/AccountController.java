package net.desmond.bankingApp.controller;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController //ensures JSON response
@RequestMapping("/api/accounts")
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    //add account rest api
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody Map<String, Object> requestData) throws Exception {
        return new ResponseEntity<>(accountService.createAccount(requestData), HttpStatus.CREATED);
    }

    //get account rest api
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) throws Exception {
        AccountDto foundAccount = accountService.getAccountById(id);
        return ResponseEntity.ok(foundAccount);
    }

    //deposit amount rest api
    @PutMapping("/{id}/deposit/{amount}")
    public ResponseEntity<AccountDto> depositAmount(@PathVariable Long id, @PathVariable double amount) throws Exception {
        AccountDto depositedAccount = accountService.depositAmount(id,amount);
        return ResponseEntity.ok(depositedAccount);
    }

    //withdraw amount rest api
    @PutMapping("/{id}/withdraw/{amount}")
    public ResponseEntity<AccountDto> withdrawAmount(@PathVariable Long id, @PathVariable double amount) throws Exception {
        AccountDto withdrawedAccount = accountService.withdrawAmount(id,amount);
        return ResponseEntity.ok(withdrawedAccount);
    }

    //get All rest api
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccount() throws Exception {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    //delete rest api
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteAccountById(@PathVariable Long id){
        accountService.deleteAccountById(id);
        return ResponseEntity.ok("Account deleted successfully!");
    }

}


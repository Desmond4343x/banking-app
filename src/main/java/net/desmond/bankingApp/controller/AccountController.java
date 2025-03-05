package net.desmond.bankingApp.controller;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //ensures JSON response
@RequestMapping("/api/accounts")
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    //add account rest api
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody AccountDto accountDto){ //automatically spring converts json to java object
        return new ResponseEntity<>(accountService.createAccount(accountDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API is working");
    }

    //get account rest api
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id){
        AccountDto foundAccount = accountService.getAccountById(id);
        return ResponseEntity.ok(foundAccount);
    }

    //deposit amount rest api
    @PutMapping("/{id}/deposit/{amount}")
    public ResponseEntity<AccountDto> depositAmount(@PathVariable Long id, @PathVariable double amount){
        AccountDto depositedAccount = accountService.depositAmount(id,amount);
        return ResponseEntity.ok(depositedAccount);
    }

    //withdraw amount rest api
    @PutMapping("/{id}/withdraw/{amount}")
    public ResponseEntity<AccountDto> withdrawAmount(@PathVariable Long id, @PathVariable double amount){
        AccountDto withdrawedAccount = accountService.withdrawAmount(id,amount);
        return ResponseEntity.ok(withdrawedAccount);
    }

}


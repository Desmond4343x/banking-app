package net.desmond.bankingApp.controller;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.transactions.TransactionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController //ensures JSON response
@RequestMapping("/api/bank")
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
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) throws Exception {
        AccountDto foundAccount = accountService.getAccountById(id);
        return ResponseEntity.ok(foundAccount);
    }

    //deposit amount rest api
    @PutMapping("/accounts/{id}/deposit/{amount}")
    public ResponseEntity<AccountDto> depositAmount(@PathVariable Long id, @PathVariable double amount) throws Exception {
        AccountDto depositedAccount = accountService.depositAmount(id,amount);
        return ResponseEntity.ok(depositedAccount);
    }

    //withdraw amount rest api
    @PutMapping("/accounts/{id}/withdraw/{amount}")
    public ResponseEntity<AccountDto> withdrawAmount(@PathVariable Long id, @PathVariable double amount) throws Exception {
        AccountDto withdrawedAccount = accountService.withdrawAmount(id,amount);
        return ResponseEntity.ok(withdrawedAccount);
    }

    //get All rest api
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAllAccount() throws Exception {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    //delete rest api
    @DeleteMapping("/accounts/{id}/delete")
    public ResponseEntity<String> deleteAccountById(@PathVariable Long id){
        accountService.deleteAccountById(id);
        return ResponseEntity.ok("Account deleted successfully!");
    }

    //see all transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() throws Exception {
        return ResponseEntity.ok(accountService.getAllTransactions());
    }

    //see all sent
    @GetMapping("/transactions/{id}/sent")
    public ResponseEntity<List<TransactionDto>> getAllTransactionsSent(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getAllTransactionsSent(id));
    }

    //see all received
    @GetMapping("/transactions/{id}/received")
    public ResponseEntity<List<TransactionDto>> getAllTransactionsReceived(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getAllTransactionsReceived(id));
    }

    //sendto
    @PutMapping("/accounts/{senderId}/sendTo/{receiverId}/{amount}")
    public ResponseEntity<AccountDto> sendToAccount(@PathVariable Long senderId,@PathVariable Long receiverId, @PathVariable double amount) throws Exception {
        AccountDto senderAccount = accountService.sendToAccount(senderId,receiverId,amount);
        return ResponseEntity.ok(senderAccount);
    }

}


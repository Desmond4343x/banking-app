package net.desmond.bankingApp.controller;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.transactions.TransactionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController //ensures JSON response
@RequestMapping("/bank")
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
    @PutMapping("/accounts/deposit")
    public ResponseEntity<AccountDto> depositAmount(@RequestBody Map<String, Object> request) throws Exception {
        Long id = Long.valueOf(request.get("accountId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());
        AccountDto depositedAccount = accountService.depositAmount(id, amount);
        return ResponseEntity.ok(depositedAccount);
    }

    //withdraw amount rest api
    @PutMapping("/accounts/withdraw")
    public ResponseEntity<AccountDto> withdrawAmount(@RequestBody Map<String, Object> request) throws Exception {
        Long id = Long.valueOf(request.get("accountId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());
        AccountDto withdrawedAccount = accountService.withdrawAmount(id, amount);
        return ResponseEntity.ok(withdrawedAccount);
    }

    //get All accounts rest api
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAllAccount() throws Exception {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    //delete rest api
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<String> deleteAccountById(@PathVariable Long id){
        accountService.deleteAccountById(id);
        return ResponseEntity.ok("Account deleted successfully.");
    }

    //see all transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() throws Exception {
        return ResponseEntity.ok(accountService.getAllTransactions());
    }

    //see all trans user
    @GetMapping("/transactions/{id}")
    public ResponseEntity<List<TransactionDto>> getAllTransactionsId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getAllTransactionsId(id));
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

    // sendTo
    @PutMapping("/accounts/sendTo")
    public ResponseEntity<AccountDto> sendToAccount(@RequestBody Map<String, Object> request) throws Exception {
        Long senderId = Long.valueOf(request.get("senderId").toString());
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());

        AccountDto senderAccount = accountService.sendToAccount(senderId, receiverId, amount);
        return ResponseEntity.ok(senderAccount);
    }

    // requestFrom
    @PutMapping("/accounts/requestFrom")
    public ResponseEntity<String> requestFromAccount(@RequestBody Map<String, Object> request) throws Exception {
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        Long senderId = Long.valueOf(request.get("senderId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());

        accountService.requestFromAccount(receiverId, senderId, amount);
        return ResponseEntity.ok("Transaction is pending.");
    }

    //show all pending
    @GetMapping("/transactions/pending")
    public ResponseEntity<List<TransactionDto>> getAllPendingTransactions() throws Exception {
        return ResponseEntity.ok(accountService.getAllPendingTransactions());
    }

    //show all pending for a user
    @GetMapping("/transactions/pending/{id}")
    public ResponseEntity<List<TransactionDto>> getUserPendingTransactions(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getUserPendingTransactions(id));
    }

    //show pending sent by user
    @GetMapping("/transactions/pending/{id}/sent")
    public ResponseEntity<List<TransactionDto>> getPendingSentTransactions(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getPendingSentTransactions(id));
    }

    //show pending received by user
    @GetMapping("/transactions/pending/{id}/received")
    public ResponseEntity<List<TransactionDto>> getPendingReceivedTransactions(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(accountService.getPendingReceivedTransactions(id));
    }

    //execute pending
    @PutMapping("/transactions/pending/{transId}/execute")
    public AccountDto executePendingTransaction(@PathVariable Long transId) throws Exception {
        return accountService.executePendingTransaction(transId);
    }

    @PostMapping("/login")
    public ResponseEntity<AccountDto> matchPassword(@RequestBody Map<String, Object> request) throws Exception {
        Long id = Long.valueOf(request.get("id").toString());
        String password = request.get("password").toString();

        if (accountService.matchPassword(id, password)) {
            return ResponseEntity.ok(accountService.getAccountById(id));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

}


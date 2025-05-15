package net.desmond.bankingApp.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.transactions.TransactionDto;
import net.desmond.bankingApp.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController //ensures JSON response
@RequestMapping("/bank")
public class AccountController {

    private AccountService accountService;
    private AccountRepository accountRepository;

    public AccountController(AccountService accountService,AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    //add account rest api
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody Map<String, Object> requestData) throws Exception {
        return new ResponseEntity<>(accountService.createAccount(requestData), HttpStatus.CREATED);
    }

    //get account rest api
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDto> getAccountById(@RequestHeader(value = "Authorization", required = false) String token,
                                                     @PathVariable Long id) throws Exception {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getAccountById(targetId));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //deposit amount rest api
    @PutMapping("/accounts/deposit")
    public ResponseEntity<AccountDto> depositAmount(@RequestHeader(value = "Authorization", required = false) String token,
                                                     @RequestBody Map<String, Object> request) throws Exception {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long userId = JwtUtil.extractUserId(jwt);
            double amount = Double.parseDouble(request.get("amount").toString());
            AccountDto depositedAccount = accountService.depositAmount(userId, amount);
            return ResponseEntity.ok(depositedAccount);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //withdraw amount rest api
    @PutMapping("/accounts/withdraw")
    public ResponseEntity<AccountDto> withdrawAmount(@RequestHeader(value = "Authorization", required = false) String token,
                                                     @RequestBody Map<String, Object> request) throws Exception {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long userId = JwtUtil.extractUserId(jwt);
            double amount = Double.parseDouble(request.get("amount").toString());
            AccountDto withdrawedAccount = accountService.withdrawAmount(userId, amount);
            return ResponseEntity.ok(withdrawedAccount);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //get All accounts rest api
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAllAccounts(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);
            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Forbidden if not an admin
            }
            return ResponseEntity.ok(accountService.getAllAccounts());

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //delete rest api
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<String> deleteAccountById(@RequestHeader(value = "Authorization", required = false) String token,
                                                    @PathVariable Long id) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);
            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Not an Admin");
            }
            accountService.deleteAccountById(id);
            return ResponseEntity.ok("Account deleted successfully.");

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //see all transactions
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);
            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Not an Admin");
            }
            return ResponseEntity.ok(accountService.getAllTransactions());

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //see all trans user
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getAllTransactionsId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getAllTransactionsId(targetId));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //see all sent
    @GetMapping("/transactions/{id}/sent")
    public ResponseEntity<?> getAllTransactionsSent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getAllTransactionsSent(targetId));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //see all received
    @GetMapping("/transactions/{id}/received")
    public ResponseEntity<?> getAllTransactionsReceived(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getAllTransactionsReceived(targetId));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    @PutMapping("/accounts/sendTo")
    public ResponseEntity<?> sendToAccount(@RequestHeader(value = "Authorization", required = false) String token,
                                           @RequestBody Map<String, Object> request) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long senderId = JwtUtil.extractUserId(jwt);
            Long receiverId = Long.valueOf(request.get("receiverId").toString());
            double amount = Double.parseDouble(request.get("amount").toString());

            AccountDto senderAccount = accountService.sendToAccount(senderId, receiverId, amount);
            return ResponseEntity.ok(senderAccount);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    // requestFrom
    @PutMapping("/accounts/requestFrom")
    public ResponseEntity<?> requestFromAccount(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long receiverId = JwtUtil.extractUserId(jwt);

            Long senderId = Long.valueOf(request.get("senderId").toString());
            double amount = Double.parseDouble(request.get("amount").toString());

            accountService.requestFromAccount(receiverId, senderId, amount);
            return ResponseEntity.ok("Transaction is pending.");

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //show all pending
    @GetMapping("/transactions/pending")
    public ResponseEntity<?> getAllPendingTransactions(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);

            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Only admins can view all pending transactions.");
            }

            List<TransactionDto> transactions = accountService.getAllPendingTransactions();
            return ResponseEntity.ok(transactions);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //show all pending for a user
    @GetMapping("/transactions/pending/{id}")
    public ResponseEntity<?> getUserPendingTransactions(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getUserPendingTransactions(targetId));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    //show pending to be sent by user
    @GetMapping("/transactions/pending/{id}/sent")
    public ResponseEntity<?> getPendingSentTransactions(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getPendingSentTransactions(targetId));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //show pending to be received by user
    @GetMapping("/transactions/pending/{id}/received")
    public ResponseEntity<?> getPendingReceivedTransactions(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String role = JwtUtil.extractRole(jwt);

            Long targetId = "user".equals(role) ? jwtUserId : id;
            return ResponseEntity.ok(accountService.getPendingReceivedTransactions(targetId));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    //execute pending
    @PutMapping("/transactions/pending/{transId}/execute")
    public ResponseEntity<?> executePendingTransaction(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long transId) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token");
            }

            String jwt = token.replace("Bearer ", "");
            JwtUtil.extractUserId(jwt); // this will throw exception if JWT is invalid

            AccountDto result = accountService.executePendingTransaction(transId);
            return ResponseEntity.ok(result);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token. Please login again.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> matchPassword(@RequestBody Map<String, Object> request) throws Exception {
        Long id = Long.valueOf(request.get("id").toString());
        String password = request.get("password").toString();

        if (accountService.matchPassword(id, password)) {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

            String token = JwtUtil.generateToken(id, account.getRole());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

}


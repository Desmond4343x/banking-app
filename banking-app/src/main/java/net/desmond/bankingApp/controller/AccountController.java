package net.desmond.bankingApp.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountCred;
import net.desmond.bankingApp.mapper.Mapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.transactions.TransactionDto;
import net.desmond.bankingApp.utils.EmailService;
import net.desmond.bankingApp.utils.HashingUtil;
import net.desmond.bankingApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "https://silverstone-dun.vercel.app")
@RestController //ensures JSON response
@RequestMapping("/bank")
public class AccountController {

    @Autowired
    private EmailService emailService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    private AccountService accountService;
    private AccountRepository accountRepository;
    private AccountCredRepository accountCredRepository;

    public AccountController(AccountService accountService,AccountRepository accountRepository,AccountCredRepository accountCredRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.accountCredRepository = accountCredRepository;
    }

    @Value("${BACKEND_URL}")
    private String backendUrl;

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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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

    //get account only with jwt
    @GetMapping("/account")
    public ResponseEntity<AccountDto> getAccountByJwt(@RequestHeader(value = "Authorization", required = false) String token) throws Exception {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            String jwt = token.replace("Bearer ", "");
            Long targetId = JwtUtil.extractUserId(jwt);
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);
            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Not an Admin.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            String jwt = token.replace("Bearer ", "");
            String role = JwtUtil.extractRole(jwt);
            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Not an Admin.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
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
    @PutMapping("/transactions/pending/execute")
    public ResponseEntity<?> executePendingTransaction(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            Long transId = Long.valueOf(request.get("transId").toString());
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

    //decline request
    @PutMapping("/transactions/pending/decline")
    public ResponseEntity<?> declinePendingTransaction(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            Long transId = Long.valueOf(request.get("transId").toString());
            String jwt = token.replace("Bearer ", "");
            JwtUtil.extractUserId(jwt); //exception if invalid

            AccountDto result = accountService.declinePendingTransaction(transId);
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
        Long id;
        String password = request.get("password").toString();

        if(request.containsKey("email")){
            id=accountService.findIdByEmail(request.get("email").toString().trim().toLowerCase());
        } else {
            id = Long.valueOf(request.get("id").toString());
        }

        if (accountService.matchPassword(id, password)) {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));
            Account decrypted = Mapper.mapToDecryptedAccount(account,accountCredRepository);

            if (!decrypted.getVerificationStatus().equalsIgnoreCase("verified")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not verified yet.");
            }

            String token = JwtUtil.generateToken(id, decrypted.getRole());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("id") Long id, @RequestParam("token") String token)  throws Exception {
        if (accountService.matchToken(id, token)) {
            accountService.markAsVerified(id);
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired verification link.");
        }
    }

    @DeleteMapping("/transaction/{transId}")
    public ResponseEntity<String> deleteTransactionById(@RequestHeader(value = "Authorization", required = false) String token,
                                                    @PathVariable Long transId) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            String jwt = token.replace("Bearer ", "");

            accountService.deleteTransactionById(transId);
            return ResponseEntity.ok("Transaction deleted successfully.");

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

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, Object> request) throws Exception {
        accountService.setTemporaryPassword(request);
        return ResponseEntity.ok("Temporary password has been sent to your verified email.");
    }

    @PostMapping("/change-address")
    public ResponseEntity<?> changeAddress(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, String> body) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String newAddress = body.get("address");

            if (newAddress == null || newAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("detail", "Address cannot be empty."));
            }

            Account account = accountRepository.findById(jwtUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));
            Account decrypted = Mapper.mapToDecryptedAccount(account,accountCredRepository);

            decrypted.setAccountHolderAddress(newAddress);
            accountRepository.save(Mapper.mapToEncryptedAccount(decrypted,accountCredRepository));

            return ResponseEntity.ok(Map.of("message", "Address updated successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("detail", "Failed to update address."));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token,
                                            @RequestBody Map<String, String> body) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("detail", "Current and new passwords cannot be empty."));
            }

            boolean isMatch = accountService.matchPassword(jwtUserId, currentPassword);
            if (!isMatch) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("detail", "Current password is incorrect."));
            }

            AccountCred account = accountCredRepository.findById(jwtUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

            String hashedNewPassword = HashingUtil.hashPassword(newPassword);
            account.setHashedUserPassword(hashedNewPassword);
            accountCredRepository.save(account);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("detail", "Failed to update password."));
        }
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestHeader("Authorization") String token,
                                         @RequestBody Map<String, String> body) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long jwtUserId = JwtUtil.extractUserId(jwt);
            String newEmail = body.get("email");

            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("detail", "Email address cannot be empty."));
            }

            newEmail = newEmail.trim().toLowerCase();

            // Check if new email is already in use by another account
            Long existingId = null;
            try {
                existingId = accountService.findIdByEmail(newEmail);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    throw e; // Re-throw other errors
                }
            }

            if (existingId != null && !Objects.equals(existingId, jwtUserId)) {
                return ResponseEntity.badRequest().body(Map.of("detail", "This email is already registered with another account."));
            }

            if (Objects.equals(existingId, jwtUserId)) {
                return ResponseEntity.badRequest().body(Map.of("detail", "New Email address cannot be same as current Email address."));
            }

            // Proceed with email update and verification
            Account account = accountRepository.findById(jwtUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

            Account decrypted = Mapper.mapToDecryptedAccount(account, accountCredRepository);
            decrypted.setAccountHolderEmailAddress(newEmail);

            String verificationToken = UUID.randomUUID().toString();
            decrypted.setVerificationStatus(verificationToken); // unverified

            String link = backendUrl+"/bank/verify?id=" +decrypted.getAccountId()
                    + "&token=" + URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);

            emailService.sendVerificationEmail(
                    newEmail,
                    "Silverstone: Email Verification",
                    "\nClick this link to verify your email: " + link + "\n\nFrom Silverstone Support Team"
            );

            accountRepository.save(Mapper.mapToEncryptedAccount(decrypted, accountCredRepository));

            return ResponseEntity.ok(Map.of("message", "Verification mail has been sent to the new Email address. Please verify to activate it."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("detail", "Failed to update email address."));
        }
    }

    @GetMapping("/is-admin")
    public ResponseEntity<?> isAdmin(
            @RequestHeader(value = "Authorization",required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed token.");
            }

            String jwt = token.substring(7);
            String role = JwtUtil.extractRole(jwt);

            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Only admins can view all pending transactions.");
            }

            return ResponseEntity.ok(true);

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

}


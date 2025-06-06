package net.desmond.bankingApp.service.impl;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountCred;
import net.desmond.bankingApp.mapper.Mapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.transactions.Transaction;
import net.desmond.bankingApp.transactions.TransactionDto;
import net.desmond.bankingApp.transactions.TransactionRepository;
import net.desmond.bankingApp.utils.EncryptionUtil;
import net.desmond.bankingApp.utils.HashingUtil;
import net.desmond.bankingApp.utils.KeyGeneratorUtil;
import net.desmond.bankingApp.utils.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;
    private AccountCredRepository accountCredRepository; //can be considered the only dependency for decrypting everything, as without this whole data cannot be accessed.
    private TransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    public AccountServiceImpl(AccountRepository accountRepository, AccountCredRepository accountCredRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.accountCredRepository=accountCredRepository;
        this.transactionRepository = transactionRepository;
    }

    @Value("${BACKEND_URL}")
    private String backendUrl;

    @Override
    public AccountDto createAccount(Map<String, Object> requestData) throws Exception { //map of data from user, converted to accountDto
        String accountHolderName = requestData.get("accountHolderName").toString().trim();
        String balance = requestData.get("balance").toString().trim();
        String accountHolderAddress = requestData.get("accountHolderAddress").toString().trim();
        String accountHolderEmailAddress = requestData.get("accountHolderEmailAddress").toString().trim().toLowerCase();
        String role = requestData.get("accountHolderRole").toString().toLowerCase().trim();

        //email check
        List<AccountDto> allAccounts = getAllAccounts();
        boolean emailExists = allAccounts.stream()
                .anyMatch(acc -> acc.getAccountHolderEmailAddress().equals(accountHolderEmailAddress));

        if (emailExists) {
            throw new IllegalArgumentException("Account with this Email already exists.");
        }

        AccountDto accountDto = new AccountDto();
        accountDto.setAccountHolderName(accountHolderName);
        accountDto.setBalance(Double.valueOf(balance));
        accountDto.setAccountHolderAddress(accountHolderAddress);
        accountDto.setAccountHolderEmailAddress(accountHolderEmailAddress);

        //x---x---x--x---x--- till here only user entered data DTO data formatting

        Account account = Mapper.mapToAccount(accountDto);

        //key generation
        SecretKey aesKey = KeyGeneratorUtil.generateAESKey();
        KeyPair rsaKeyPair = KeyGeneratorUtil.generateRSAKeyPair();
        PublicKey publicKey = rsaKeyPair.getPublic();
        PrivateKey privateKey = rsaKeyPair.getPrivate();

        String encryptedAesKey = EncryptionUtil.encryptAESKeyWithRSA(aesKey, publicKey);
        account.setAesEncryptedKey(encryptedAesKey);
        account.setRsaPublicKey(KeyGeneratorUtil.encodeKeyToBase64(publicKey));
        account.setRole(role);
        account.setVerificationStatus(UUID.randomUUID().toString());
        Account savedAccount = accountRepository.save(account);
        //acc saved unencrypted

        //copy of saved for returning
        Account saved = new Account(savedAccount);

        String password = (String) requestData.get("password");
        AccountCred accountCred = new AccountCred();
        accountCred.setId(savedAccount.getAccountId());
        accountCred.setHashedUserPassword(HashingUtil.hashPassword(password));
        accountCred.setRsaPrivateKey(KeyGeneratorUtil.encodeKeyToBase64(privateKey));
        accountCredRepository.save(accountCred);

        //encryption in mapper class
        Account encryptedAccount = Mapper.mapToEncryptedAccount(savedAccount,accountCredRepository);
        Account savedEncryptedAccount = accountRepository.save(encryptedAccount);

        //send verification with unencrypted token
        Account decrypted = Mapper.mapToDecryptedAccount(encryptedAccount,accountCredRepository);
        String link = backendUrl+"/bank/verify?id=" + decrypted.getAccountId() + "&token=" +  URLEncoder.encode(decrypted.getVerificationStatus(), StandardCharsets.UTF_8);
        emailService.sendVerificationEmail(decrypted.getAccountHolderEmailAddress(),
                "Silverstone: Email Verification",
                "\nClick this link to verify your email: " + link+"\nfrom Silverstone Support Team");

        return Mapper.mapToAccountDto(saved);
    }

    @Override
    public AccountDto getAccountById(Long id) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        return Mapper.mapToAccountDto(decryptedAccount);
    }

    @Override
    public AccountDto depositAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        double curAmount = Double.valueOf(decryptedAccount.getBalance());
        curAmount+=amount;
        decryptedAccount.setBalance(String.valueOf(curAmount));

        Account encryptedAccount = Mapper.mapToEncryptedAccount(decryptedAccount,accountCredRepository);

        Transaction transaction =  new Transaction(id,id,String.valueOf(amount),"deposit",encryptedAccount.getAesEncryptedKey());
        transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction,accountCredRepository));

        return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(accountRepository.save(encryptedAccount),accountCredRepository));
    }

    @Override
    public AccountDto withdrawAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        double curAmount = Double.valueOf(decryptedAccount.getBalance());

        if (curAmount < amount) {
            Transaction transaction =  new Transaction(id,id,String.valueOf(amount),"withdraw failed", foundAccount.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction,accountCredRepository));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance.");
        }else {
            curAmount-=amount;
            decryptedAccount.setBalance(String.valueOf(curAmount));

            Account encryptedAccount = Mapper.mapToEncryptedAccount(decryptedAccount,accountCredRepository);

            Transaction transaction =  new Transaction(id,id,String.valueOf(amount),"withdraw",encryptedAccount.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction,accountCredRepository));

            return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(accountRepository.save(encryptedAccount),accountCredRepository));
        }
    }

    @Override
    public List<AccountDto> getAllAccounts() throws Exception {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDto> accountDtos = new ArrayList<>();
        for(Account ac : accounts){
            accountDtos.add(Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(ac,accountCredRepository)));
        }
        return accountDtos;
    }

    @Override
    public void deleteAccountById(Long id) {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

        accountRepository.deleteById(id);
        accountCredRepository.deleteById(id);
    }

    @Override
    public List<TransactionDto> getAllTransactions() throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getAllTransactionsId(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findTransactionById(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getAllTransactionsSent(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllSent(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getAllTransactionsReceived(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllReceived(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public AccountDto sendToAccount(Long senderId, Long receiverId, Double amount) throws Exception {
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender Account does not exist."));
        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver Account does not exist."));

        Account decryptedSenderAccount = Mapper.mapToDecryptedAccount(sender, accountCredRepository);
        Account decryptedReceiverAccount = Mapper.mapToDecryptedAccount(receiver, accountCredRepository);
        double curAmountSender = Double.valueOf(decryptedSenderAccount.getBalance());
        double curAmountReceiver = Double.valueOf(decryptedReceiverAccount.getBalance());

        if (curAmountSender < amount) {
            Transaction transaction = new Transaction(senderId, receiverId, String.valueOf(amount), "failed", sender.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction, accountCredRepository));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance.");
        } else {
            curAmountSender -= amount;
            curAmountReceiver += amount;
            decryptedSenderAccount.setBalance(String.valueOf(curAmountSender));
            decryptedReceiverAccount.setBalance(String.valueOf(curAmountReceiver));

            Account encryptedSenderAccount = Mapper.mapToEncryptedAccount(decryptedSenderAccount, accountCredRepository);
            Account encryptedReceiverAccount = Mapper.mapToEncryptedAccount(decryptedReceiverAccount, accountCredRepository);

            Transaction transaction = new Transaction(senderId, receiverId, String.valueOf(amount), "success", encryptedSenderAccount.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction, accountCredRepository));

            Account savedEncryptedAccount = accountRepository.save(encryptedSenderAccount);
            accountRepository.save(encryptedReceiverAccount);
            return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(savedEncryptedAccount, accountCredRepository));
        }
    }

    @Override
    public void requestFromAccount(Long receiverId, Long senderId, Double amount) throws Exception{
        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver Account does not exist."));
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender Account does not exist."));

        Transaction transaction = new Transaction(senderId, receiverId, String.valueOf(amount), "pending", sender.getAesEncryptedKey());
        transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction, accountCredRepository));
    }

    @Override
    public List<TransactionDto> getAllPendingTransactions() throws Exception {
        List<Transaction> transactions = transactionRepository.findAllPending();
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getUserPendingTransactions(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllPendingByUser(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getPendingSentTransactions(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllPendingSentByUser(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getPendingReceivedTransactions(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllPendingReceivedByUser(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto
                    (Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public AccountDto executePendingTransaction(Long transId) throws Exception {
        Transaction transaction = transactionRepository.findById(transId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction does not exist."));

        if (!transaction.getStatus().equals("pending")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction is not pending.");
        }

        Account receiver = accountRepository.findById(transaction.getReceiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver Account does not exist."));
        Account sender = accountRepository.findById(transaction.getSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender Account does not exist."));

        Account decryptedReceiver = Mapper.mapToDecryptedAccount(receiver, accountCredRepository);
        Account decryptedSender = Mapper.mapToDecryptedAccount(sender, accountCredRepository);
        Transaction decryptedTransaction = Mapper.mapToDecryptedTransaction(transaction,accountCredRepository);

        double receiverBalance = Double.valueOf(decryptedReceiver.getBalance());
        double senderBalance = Double.valueOf(decryptedSender.getBalance());
        double amount = Double.valueOf(decryptedTransaction.getAmount());

        if(amount>senderBalance){
            Transaction failed = new Transaction(transaction.getSenderId(), transaction.getReceiverId(), String.valueOf(amount), "failed", sender.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(failed, accountCredRepository));
            transactionRepository.deleteById(transId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance.");
        }else{
            senderBalance -= amount;
            receiverBalance += amount;
            decryptedSender.setBalance(String.valueOf(senderBalance));
            decryptedReceiver.setBalance(String.valueOf(receiverBalance));

            Account encryptedSenderAccount = Mapper.mapToEncryptedAccount(decryptedSender, accountCredRepository);
            Account encryptedReceiverAccount = Mapper.mapToEncryptedAccount(decryptedReceiver, accountCredRepository);

            Transaction success = new Transaction(transaction.getSenderId(), transaction.getReceiverId(), String.valueOf(amount), "success", encryptedSenderAccount.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(success, accountCredRepository));
            transactionRepository.deleteById(transId);

            Account savedEncryptedAccount = accountRepository.save(encryptedSenderAccount);
            accountRepository.save(encryptedReceiverAccount);
            return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(savedEncryptedAccount, accountCredRepository));
        }
    }

    @Override
    public AccountDto declinePendingTransaction(Long transId) throws Exception {
        Transaction transaction = transactionRepository.findById(transId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction does not exist."));

        if (!transaction.getStatus().equals("pending")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction is not pending.");
        }

        Account receiver = accountRepository.findById(transaction.getReceiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver Account does not exist."));
        Account sender = accountRepository.findById(transaction.getSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender Account does not exist."));

        Transaction decryptedTransaction = Mapper.mapToDecryptedTransaction(transaction,accountCredRepository);
        double amount = Double.valueOf(decryptedTransaction.getAmount());

        Transaction declined = new Transaction(transaction.getSenderId(), transaction.getReceiverId(), String.valueOf(amount), "declined", sender.getAesEncryptedKey());
        transactionRepository.save(Mapper.mapToEncryptedTransaction(declined, accountCredRepository));
        transactionRepository.deleteById(transId);

        return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(sender, accountCredRepository));
    }

    @Override
    public boolean matchPassword(Long id, String pass) throws Exception {
        AccountCred acc = accountCredRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));

        return HashingUtil.verifyPassword(pass,acc.getHashedUserPassword());
    }

    @Override
    public void deleteTransactionById(Long transId) {
        Transaction foundTrans = transactionRepository.findById(transId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction does not exist."));

        transactionRepository.deleteById(transId);
    }

    @Override
    public Long findIdByEmail(String email) throws Exception {
        List<AccountDto> allAccounts = getAllAccounts();

        for (AccountDto dto : allAccounts) {
            if (dto.getAccountHolderEmailAddress().equalsIgnoreCase(email.trim())) {
                return dto.getAccountId();
            }
        }
        return null;
    }

    @Override
    public boolean matchToken(Long id, String token) throws Exception {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));

        Account decrypted = Mapper.mapToDecryptedAccount(account,accountCredRepository);

        return decrypted.getVerificationStatus().equals(token);
    }

    @Override
    public void markAsVerified(Long id) throws Exception {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));

        Account decrypted = Mapper.mapToDecryptedAccount(account,accountCredRepository);
        decrypted.setVerificationStatus("verified");
        Account encrypted = Mapper.mapToEncryptedAccount(decrypted,accountCredRepository);
        accountRepository.save(encrypted);
    }

    @Override
    public void setTemporaryPassword(Map<String, Object> request) throws Exception {
        Long id;

        if (request.containsKey("email")) {
            String email = request.get("email").toString().trim().toLowerCase();
            id = findIdByEmail(email);
        } else {
            id = Long.valueOf(request.get("id").toString());
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist."));
        Account decrypted = Mapper.mapToDecryptedAccount(account,accountCredRepository);

        if (!"verified".equalsIgnoreCase(decrypted.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is unverified. Please verify account first.");
        }

        String tempPassword = generateRandomPassword(16);

        AccountCred accountCred = accountCredRepository.getReferenceById(id);
        accountCred.setHashedUserPassword(HashingUtil.hashPassword(tempPassword));
        accountCredRepository.save(accountCred);

        emailService.sendVerificationEmail(decrypted.getAccountHolderEmailAddress(),
                "Silverstone - Temporary Password", "\nYour temporary password is: " + tempPassword +
                "\nUse this to log in and update your password immediately.\n\nfrom Silverstone Support Team");

    }

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}

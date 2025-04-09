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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;
    private AccountCredRepository accountCredRepository; //can be considered the only dependency for decrypting everything, as without this whole data cannot be accessed.
    private TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository, AccountCredRepository accountCredRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.accountCredRepository=accountCredRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountDto createAccount(Map<String, Object> requestData) throws Exception { //map of data from user, converted to accountDto
        String accountHolderName = requestData.get("accountHolderName").toString();
        String balance = requestData.get("balance").toString();
        String accountHolderAddress = requestData.get("accountHolderAddress").toString();
        String accountHolderEmailAddress = requestData.get("accountHolderEmailAddress").toString();

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
        Account savedAccount = accountRepository.save(account);
        //acc saved unencrypted

        //copy of saved for returning
        Account saved = new Account(savedAccount);

        String password = (String) requestData.get("password");
        AccountCred accountCred = new AccountCred();
        accountCred.setId(savedAccount.getAccountId());
        accountCred.setHashedUserPassword(HashingUtil.hashPassword(password, HashingUtil.generateSalt()));
        accountCred.setRsaPrivateKey(KeyGeneratorUtil.encodeKeyToBase64(privateKey));
        accountCredRepository.save(accountCred);

        //encryption in mapper class
        Account encryptedAccount = Mapper.mapToEncryptedAccount(savedAccount,accountCredRepository);
        Account savedEncryptedAccount = accountRepository.save(encryptedAccount);

        return Mapper.mapToAccountDto(saved);
    }

    @Override
    public AccountDto getAccountById(Long id) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        return Mapper.mapToAccountDto(decryptedAccount);
    }

    @Override
    public AccountDto depositAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        double curAmount = Double.valueOf(decryptedAccount.getBalance());
        curAmount+=amount;
        decryptedAccount.setBalance(String.valueOf(curAmount));

        Account encryptedAccount = Mapper.mapToEncryptedAccount(decryptedAccount,accountCredRepository);

        Transaction transaction =  new Transaction(id,id,String.valueOf(amount),"success",encryptedAccount.getAesEncryptedKey());
        transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction,accountCredRepository));

        return Mapper.mapToAccountDto(Mapper.mapToDecryptedAccount(accountRepository.save(encryptedAccount),accountCredRepository));
    }

    @Override
    public AccountDto withdrawAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        Account decryptedAccount = Mapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        double curAmount = Double.valueOf(decryptedAccount.getBalance());

        if (curAmount < amount) {
            Transaction transaction =  new Transaction(id,id,String.valueOf(-amount),"failed", foundAccount.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction,accountCredRepository));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance");
        }else {
            curAmount-=amount;
            decryptedAccount.setBalance(String.valueOf(curAmount));

            Account encryptedAccount = Mapper.mapToEncryptedAccount(decryptedAccount,accountCredRepository);

            Transaction transaction =  new Transaction(id,id,String.valueOf(-amount),"success",encryptedAccount.getAesEncryptedKey());
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
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        accountRepository.deleteById(id);
        accountCredRepository.deleteById(id);
    }

    @Override
    public List<TransactionDto> getAllTransactions() throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto(Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getAllTransactionsSent(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllSent(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto(Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public List<TransactionDto> getAllTransactionsReceived(Long id) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllReceived(id);
        List<TransactionDto> decryptedTransactions = new ArrayList<>();
        for(Transaction tr : transactions){
            decryptedTransactions.add(Mapper.mapToTransactionDto(Mapper.mapToDecryptedTransaction(tr,accountCredRepository)));
        }
        return decryptedTransactions;
    }

    @Override
    public AccountDto sendToAccount(Long senderId, Long receiverId, Double amount) throws Exception {
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender Account does not exist"));
        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver Account does not exist"));

        Account decryptedSenderAccount = Mapper.mapToDecryptedAccount(sender, accountCredRepository);
        Account decryptedReceiverAccount = Mapper.mapToDecryptedAccount(receiver, accountCredRepository);
        double curAmountSender = Double.valueOf(decryptedSenderAccount.getBalance());
        double curAmountReceiver = Double.valueOf(decryptedReceiverAccount.getBalance());

        if (curAmountSender < amount) {
            Transaction transaction = new Transaction(senderId, receiverId, String.valueOf(amount), "failed", sender.getAesEncryptedKey());
            transactionRepository.save(Mapper.mapToEncryptedTransaction(transaction, accountCredRepository));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance");
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
}

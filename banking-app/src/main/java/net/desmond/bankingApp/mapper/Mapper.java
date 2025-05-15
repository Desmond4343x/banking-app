package net.desmond.bankingApp.mapper;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.transactions.Transaction;
import net.desmond.bankingApp.transactions.TransactionDto;
import net.desmond.bankingApp.utils.EncryptionUtil;
import net.desmond.bankingApp.utils.KeyGeneratorUtil;


import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mapper {

    public static Account mapToAccount(AccountDto accountDto){
        return new Account(
                accountDto.getAccountId(),
                accountDto.getAccountHolderName(),
                String.valueOf(accountDto.getBalance()),
                accountDto.getAccountHolderAddress(),
                accountDto.getAccountHolderEmailAddress()
        );
    }

    public static AccountDto mapToAccountDto(Account account){
        return new AccountDto(
                account.getAccountId(),
                account.getAccountHolderName(),
                Double.valueOf(account.getBalance()),
                account.getAccountHolderAddress(),
                account.getAccountHolderEmailAddress()
        );
    }

    public static Account mapToEncryptedAccount(Account account, AccountCredRepository accountCredRepository) throws Exception {
        Account encryptedAccount = new Account();

        //decrypt key
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(account.getAccountId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(account.getAesEncryptedKey(), privateKey);

        //encrypt
        String encryptedName = EncryptionUtil.encryptWithAES(account.getAccountHolderName(), aesKey);
        String encryptedBalance = EncryptionUtil.encryptWithAES(account.getBalance(), aesKey);
        String encryptedAddress = EncryptionUtil.encryptWithAES(account.getAccountHolderAddress(), aesKey);
        String encryptedEmailAddress = EncryptionUtil.encryptWithAES(account.getAccountHolderEmailAddress(), aesKey);

        //assign values
        encryptedAccount.setAccountId(account.getAccountId());
        encryptedAccount.setAccountHolderName(encryptedName);
        encryptedAccount.setBalance(encryptedBalance);
        encryptedAccount.setAccountHolderAddress(encryptedAddress);
        encryptedAccount.setAccountHolderEmailAddress(encryptedEmailAddress);
        encryptedAccount.setAesEncryptedKey(account.getAesEncryptedKey());
        encryptedAccount.setRsaPublicKey(account.getRsaPublicKey());
        encryptedAccount.setRole(account.getRole());

        return encryptedAccount;
    }

    public static Account mapToDecryptedAccount(Account account, AccountCredRepository accountCredRepository) throws Exception {
        Account decryptedAccount = new Account();

        //decrypt key
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(account.getAccountId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(account.getAesEncryptedKey(), privateKey);

        //decrypt
        String decryptedName = EncryptionUtil.decryptWithAES(account.getAccountHolderName(), aesKey);
        String decryptedBalance = EncryptionUtil.decryptWithAES(account.getBalance(), aesKey);
        String decryptedAddress = EncryptionUtil.decryptWithAES(account.getAccountHolderAddress(), aesKey);
        String decryptedEmailAddress = EncryptionUtil.decryptWithAES(account.getAccountHolderEmailAddress(), aesKey);


        //assign values
        decryptedAccount.setAccountId(account.getAccountId());
        decryptedAccount.setAccountHolderName(decryptedName);
        decryptedAccount.setBalance(decryptedBalance);
        decryptedAccount.setAccountHolderAddress(decryptedAddress);
        decryptedAccount.setAccountHolderEmailAddress(decryptedEmailAddress);
        decryptedAccount.setAesEncryptedKey(account.getAesEncryptedKey());
        decryptedAccount.setRsaPublicKey(account.getRsaPublicKey());
        decryptedAccount.setRole(account.getRole());

        return decryptedAccount;
    }

    public static TransactionDto mapToTransactionDto(Transaction transaction){
        return new TransactionDto(
                transaction.getTransId(),
                transaction.getSenderId(),
                transaction.getReceiverId(),
                Double.valueOf(transaction.getAmount()),
                transaction.getStatus(),
                LocalDateTime.parse(transaction.getTimestamp()).format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
        );
    }

    public static Transaction mapToEncryptedTransaction(Transaction transaction, AccountCredRepository accountCredRepository) throws Exception {
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(transaction.getSenderId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(transaction.getSenderAesEncryptedKey(), privateKey);

        String encryptedAmount = EncryptionUtil.encryptWithAES(transaction.getAmount(), aesKey);

        return new Transaction(
                transaction.getSenderId(),
                transaction.getReceiverId(),
                encryptedAmount,
                transaction.getStatus(),
                transaction.getSenderAesEncryptedKey()
        );
    }

    public static Transaction mapToDecryptedTransaction(Transaction transaction, AccountCredRepository accountCredRepository) throws Exception {
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(transaction.getSenderId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(transaction.getSenderAesEncryptedKey(), privateKey);

        String decryptedAmount = EncryptionUtil.decryptWithAES(transaction.getAmount(), aesKey);

        return new Transaction(
                transaction.getTransId(),
                transaction.getSenderId(),
                transaction.getReceiverId(),
                decryptedAmount,
                transaction.getStatus(),
                transaction.getTimestamp(),
                transaction.getSenderAesEncryptedKey()
        );
    }

}
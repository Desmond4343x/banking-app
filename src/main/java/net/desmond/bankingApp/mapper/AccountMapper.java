package net.desmond.bankingApp.mapper;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountDetails;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.utils.EncryptionUtil;
import net.desmond.bankingApp.utils.KeyGeneratorUtil;


import javax.crypto.SecretKey;
import java.security.PrivateKey;

public class AccountMapper {

    public static Account mapToAccount(AccountDto accountDto){
        AccountDetails accountDetails = new AccountDetails(
                accountDto.getAccountHolderName(),
                accountDto.getBalance()
        );

        return new Account(
                accountDto.getId(),
                accountDetails
        );
    }

    public static AccountDto mapToAccountDto(Account account){
        AccountDetails details = account.getAccountDetails();

        return new AccountDto(
                account.getId(),
                details.getAccountHolderName(),
                details.getBalance()
        );
    }

    public static Account mapToEncryptedAccount(Account account, AccountCredRepository accountCredRepository) throws Exception {
        Account encryptedAccount = new Account();

        //decrypt key
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(account.getId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(account.getAesEncryptedKey(), privateKey);

        //encrypt
        String encryptedName = EncryptionUtil.encryptWithAES(account.getAccountDetails().getAccountHolderName(), aesKey);
        //String encryptedBalance = EncryptionUtil.encryptWithAES(String.valueOf(account.getAccountDetails().getBalance()), aesKey);

        //assign values
        encryptedAccount.setId(account.getId());
        encryptedAccount.setAccountDetails(new AccountDetails(encryptedName,account.getAccountDetails().getBalance()));
        encryptedAccount.setAesEncryptedKey(account.getAesEncryptedKey());
        encryptedAccount.setRsaPublicKey(account.getRsaPublicKey());

        return encryptedAccount;
    }

    public static Account mapToDecryptedAccount(Account account,  AccountCredRepository accountCredRepository) throws Exception {
        Account decryptedAccount = new Account();

        //decrypt key
        String base64PrivateKey = accountCredRepository.findPrivateKeyByAccountId(account.getId());
        PrivateKey privateKey = KeyGeneratorUtil.decodeBase64ToPrivateKey(base64PrivateKey);
        SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(account.getAesEncryptedKey(), privateKey);

        //decrypt
        String decryptedName = EncryptionUtil.decryptWithAES(account.getAccountDetails().getAccountHolderName(), aesKey);
        //String encryptedBalance = EncryptionUtil.encryptWithAES(String.valueOf(account.getAccountDetails().getBalance()), aesKey);

        //assign values
        decryptedAccount.setId(account.getId());
        decryptedAccount.setAccountDetails(new AccountDetails(decryptedName,account.getAccountDetails().getBalance()));
        decryptedAccount.setAesEncryptedKey(account.getAesEncryptedKey());
        decryptedAccount.setRsaPublicKey(account.getRsaPublicKey());

        return decryptedAccount;
    }
}
package net.desmond.bankingApp.service.impl;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountCred;
import net.desmond.bankingApp.mapper.AccountMapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.service.AccountService;
import net.desmond.bankingApp.utils.EncryptionUtil;
import net.desmond.bankingApp.utils.KeyGeneratorUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;
    private AccountCredRepository accountCredRepository;

    public AccountServiceImpl(AccountRepository accountRepository,AccountCredRepository accountCredRepository) {
        this.accountRepository = accountRepository;
        this.accountCredRepository=accountCredRepository;
    }

    @Override
    public AccountDto createAccount(Map<String, Object> requestData) throws Exception { //map of data from user, converted to accountDto
        String accountHolderName = (String) requestData.get("accountHolderName");
        double balance = Double.parseDouble(requestData.get("balance").toString());

        AccountDto accountDto = new AccountDto();
        accountDto.setAccountHolderName(accountHolderName);
        accountDto.setBalance(balance);

        //x---x---x--x---x--- till here only user entered data DTO data formatting

        Account account = AccountMapper.mapToAccount(accountDto);

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
        accountCred.setId(savedAccount.getId());
        accountCred.setHashedUserPassword(password); //not hashed yet
        accountCred.setRsaPrivateKey(KeyGeneratorUtil.encodeKeyToBase64(privateKey));
        accountCredRepository.save(accountCred);

        //encryption in mapper class
        Account encryptedAccount = AccountMapper.mapToEncryptedAccount(savedAccount,accountCredRepository);
        Account savedEncryptedAccount = accountRepository.save(encryptedAccount);

        return AccountMapper.mapToAccountDto(saved);
    }

    @Override
    public AccountDto getAccountById(Long id) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        Account decryptedAccount = AccountMapper.mapToDecryptedAccount(foundAccount,accountCredRepository);
        return AccountMapper.mapToAccountDto(decryptedAccount);
    }

    @Override
    public AccountDto depositAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        foundAccount.getAccountDetails().setBalance(foundAccount.getAccountDetails().getBalance()+amount);
        return AccountMapper.mapToAccountDto(AccountMapper.mapToDecryptedAccount(accountRepository.save(foundAccount),accountCredRepository));
    }

    @Override
    public AccountDto withdrawAmount(Long id, double amount) throws Exception {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        if (foundAccount.getAccountDetails().getBalance() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance");
        }else {
            foundAccount.getAccountDetails().setBalance(foundAccount.getAccountDetails().getBalance() - amount);
            return AccountMapper.mapToAccountDto(AccountMapper.mapToDecryptedAccount(accountRepository.save(foundAccount),accountCredRepository));
        }
    }

    @Override
    public List<AccountDto> getAllAccounts() throws Exception {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDto> accountDtos = new ArrayList<>();
        for(Account ac : accounts){
            accountDtos.add(AccountMapper.mapToAccountDto(AccountMapper.mapToDecryptedAccount(ac,accountCredRepository)));
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

}

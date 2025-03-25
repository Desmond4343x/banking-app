package net.desmond.bankingApp.service.impl;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountCred;
import net.desmond.bankingApp.mapper.AccountMapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.secureVault.AccountCredRepository;
import net.desmond.bankingApp.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
    public AccountDto createAccount(Map<String, Object> requestData) { //map of data from user, converted to accountDto
        String accountHolderName = (String) requestData.get("accountHolderName");
        double balance = Double.parseDouble(requestData.get("balance").toString());

        AccountDto accountDto = new AccountDto();
        accountDto.setAccountHolderName(accountHolderName);
        accountDto.setBalance(balance);
        //x---x---x--x---x--- till here only user entered data DTO data formatting
        Account account = AccountMapper.mapToAccount(accountDto);
        account.setAesEncryptedKey("ily2");
        account.setRsaPublicKey("rsaily3");

        Account savedAccount = accountRepository.save(account);

        String password = (String) requestData.get("password"); // Extract password separately

        AccountCred accountCred = new AccountCred();
        accountCred.setId(savedAccount.getId());
        accountCred.setHashedUserPassword(password); //not hashed yet
        accountCred.setRsaPrivateKey("e2e2e23a3d2");
        accountCredRepository.save(accountCred);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));
        return AccountMapper.mapToAccountDto(foundAccount);
    }

    @Override
    public AccountDto depositAmount(Long id, double amount) {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        foundAccount.getAccountDetails().setBalance(foundAccount.getAccountDetails().getBalance()+amount);
        return AccountMapper.mapToAccountDto(accountRepository.save(foundAccount));
    }

    @Override
    public AccountDto withdrawAmount(Long id, double amount) {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exist"));

        if (foundAccount.getAccountDetails().getBalance() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient Balance");
        }else {
            foundAccount.getAccountDetails().setBalance(foundAccount.getAccountDetails().getBalance() - amount);
            return AccountMapper.mapToAccountDto(accountRepository.save(foundAccount));
        }
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDto> accountDtos = new ArrayList<>();
        for(Account ac : accounts){
            accountDtos.add(AccountMapper.mapToAccountDto(ac));
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

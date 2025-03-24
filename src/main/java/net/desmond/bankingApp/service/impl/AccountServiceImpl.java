package net.desmond.bankingApp.service.impl;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.mapper.AccountMapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) { //accountDto direct from user, json
        Account account = AccountMapper.mapToAccount(accountDto);

        //generate and assign keys in account
        //generate aes, encrypt and assign
        account.setAesEncryptedKey("ily2");
        account.setRsaPublicKey("rsaily3");
        //create cred db, add password (hashed), generate pvt key and assign
        //encrypt data then modify in account

        Account savedAccount = accountRepository.save(account);

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
    }


}

package net.desmond.bankingApp.service.impl;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.mapper.AccountMapper;
import net.desmond.bankingApp.repository.AccountRepository;
import net.desmond.bankingApp.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto); //mapper required as jpa methods only take @entity
        Account savedAccount = accountRepository.save(account); //save method present in jpaRepo which accRepo inherits, default methods
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account foundAccount = accountRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Account does not exist"));
        return AccountMapper.mapToAccountDto(foundAccount);
    }
}

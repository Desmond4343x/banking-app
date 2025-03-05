package net.desmond.bankingApp.service;

import net.desmond.bankingApp.dto.AccountDto;

public interface AccountService {
    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccountById(Long id);

    AccountDto depositAmount(Long id, double amount);

    AccountDto withdrawAmount(Long id, double amount);
}

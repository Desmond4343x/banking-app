package net.desmond.bankingApp.service;

import net.desmond.bankingApp.dto.AccountDto;

public interface AccountService {
    AccountDto createAccount(AccountDto accountDto);
}

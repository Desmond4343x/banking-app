package net.desmond.bankingApp.service;

import net.desmond.bankingApp.dto.AccountDto;

import java.util.List;
import java.util.Map;

public interface AccountService {
    AccountDto createAccount(Map<String, Object> requestData);

    AccountDto getAccountById(Long id);

    AccountDto depositAmount(Long id, double amount);

    AccountDto withdrawAmount(Long id, double amount);

    List<AccountDto> getAllAccounts();

    void deleteAccountById(Long id);
}

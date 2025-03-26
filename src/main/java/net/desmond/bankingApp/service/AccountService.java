package net.desmond.bankingApp.service;

import net.desmond.bankingApp.dto.AccountDto;

import java.util.List;
import java.util.Map;

public interface AccountService {
    AccountDto createAccount(Map<String, Object> requestData) throws Exception;

    AccountDto getAccountById(Long id) throws Exception;

    AccountDto depositAmount(Long id, double amount) throws Exception;

    AccountDto withdrawAmount(Long id, double amount) throws Exception;

    List<AccountDto> getAllAccounts() throws Exception;

    void deleteAccountById(Long id);
}

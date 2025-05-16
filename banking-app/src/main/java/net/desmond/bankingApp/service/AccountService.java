package net.desmond.bankingApp.service;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.transactions.TransactionDto;

import java.util.List;
import java.util.Map;

public interface AccountService {
    AccountDto createAccount(Map<String, Object> requestData) throws Exception;

    AccountDto getAccountById(Long id) throws Exception;

    AccountDto depositAmount(Long id, double amount) throws Exception;

    AccountDto withdrawAmount(Long id, double amount) throws Exception;

    List<AccountDto> getAllAccounts() throws Exception;

    void deleteAccountById(Long id);

    List<TransactionDto> getAllTransactions() throws Exception;

    List<TransactionDto> getAllTransactionsId(Long id) throws Exception;

    List<TransactionDto> getAllTransactionsSent(Long id) throws Exception;

    List<TransactionDto> getAllTransactionsReceived(Long id) throws Exception;

    AccountDto sendToAccount(Long senderId, Long receiverId, Double amount) throws Exception;

    void requestFromAccount(Long receiverId, Long senderId, Double amount) throws Exception;

    List<TransactionDto> getAllPendingTransactions() throws Exception;

    List<TransactionDto> getUserPendingTransactions(Long id) throws Exception;

    List<TransactionDto> getPendingSentTransactions(Long id) throws Exception;

    List<TransactionDto> getPendingReceivedTransactions(Long id) throws Exception;

    AccountDto executePendingTransaction(Long transId) throws Exception;

    AccountDto declinePendingTransaction(Long transId) throws Exception;

    boolean matchPassword(Long id, String pass) throws Exception;

    void deleteTransactionById(Long transId);
}


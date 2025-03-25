package net.desmond.bankingApp.mapper;

import net.desmond.bankingApp.dto.AccountDto;
import net.desmond.bankingApp.entity.Account;
import net.desmond.bankingApp.entity.AccountDetails;

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
}



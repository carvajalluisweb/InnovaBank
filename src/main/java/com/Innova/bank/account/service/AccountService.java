package com.Innova.bank.account.service;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.dto.UpdateAccountStatusRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AccountService {

    List<AccountResponse> getMyAccounts();

    AccountResponse getMyAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAllAccounts();

    AccountResponse getAccountByAccountNumber(String accountNumber);

    AccountResponse createAccount(CreateAccountRequest request, HttpServletRequest httpRequest);

    AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request, HttpServletRequest httpRequest);
}

package com.Innova.bank.transaction.service;

import com.Innova.bank.transaction.dto.CreateDepositRequest;
import com.Innova.bank.transaction.dto.CreateTransferRequest;
import com.Innova.bank.transaction.dto.CreateWithdrawalRequest;
import com.Innova.bank.transaction.dto.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

public interface TransactionService {

    TransactionResponse createTransfer(String requestId, CreateTransferRequest request, HttpServletRequest httpRequest);

    TransactionResponse createDeposit(String requestId, CreateDepositRequest request, HttpServletRequest httpRequest);

    TransactionResponse createWithdrawal(String requestId, CreateWithdrawalRequest request, HttpServletRequest httpRequest);

    TransactionResponse reverseTransaction(String referenceNumber, HttpServletRequest httpRequest);

    Page<TransactionResponse> getMyTransactions(int page, int size);

    Page<TransactionResponse> getAccountTransactions(String accountNumber, int page, int size );
}
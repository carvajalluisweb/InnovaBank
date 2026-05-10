package com.Innova.bank.transaction.service;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.transaction.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionStatusService {

    Transaction saveSuccessTransaction(String requestId, String referenceNumber, Account originAccount, Account toAccount, BigDecimal amount,
            BigDecimal fee, TransactionType transactionType, String description);

    Transaction saveFailedTransaction(String requestId, String referenceNumber, Account originAccount, Account toAccount, BigDecimal amount,
            TransactionType transactionType, String description);

    Transaction reverseTransaction(Transaction transaction);

    void validateReversible(Transaction transaction);
}
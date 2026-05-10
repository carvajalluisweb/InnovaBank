package com.Innova.bank.transaction.service.impl;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.transaction.mapper.TransactionMapper;
import com.Innova.bank.transaction.repository.TransactionRepository;
import com.Innova.bank.transaction.service.TransactionStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionStatusServiceImpl
        implements TransactionStatusService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ExceptionFactory exceptionFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction saveSuccessTransaction(String requestId, String referenceNumber, Account originAccount, Account toAccount,
            BigDecimal amount, BigDecimal fee, TransactionType transactionType, String description) {

        Transaction transaction = transactionMapper.toEntity(
                        requestId,
                        referenceNumber,
                        originAccount,
                        toAccount,
                        amount,
                        fee,
                        transactionType,
                        TransactionStatus.SUCCESS,
                        description
                );

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction saveFailedTransaction(String requestId, String referenceNumber, Account originAccount, Account toAccount,
            BigDecimal amount, TransactionType transactionType, String description) {

        Transaction transaction = transactionMapper.toEntity(
                requestId,
                referenceNumber,
                originAccount,
                toAccount,
                amount,
                BigDecimal.ZERO,
                transactionType,
                TransactionStatus.FAILED,
                description
        );

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction reverseTransaction(Transaction transaction) {

        validateReversible(transaction);

        transaction.setStatus(TransactionStatus.REVERSED);

        return transactionRepository.save(transaction);
    }

    @Override
    public void validateReversible(Transaction transaction) {

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {

            throw exceptionFactory.badRequest( "Solo se pueden revertir transacciones exitosas");
        }

        if (transaction.getTransactionType() == TransactionType.DEPOSIT) {

            throw exceptionFactory.badRequest("Los depósitos no pueden revertirse");
        }
    }
}
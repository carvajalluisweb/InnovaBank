package com.Innova.bank.transaction.mapper;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.transaction.dto.CreateTransferRequest;
import com.Innova.bank.transaction.dto.TransactionResponse;
import com.Innova.bank.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class TransactionMapper {

    public Transaction toEntity(String requestId, Account originAccount, Account toAccount, BigDecimal amount, BigDecimal fee, TransactionType transactionType, String description){
        return Transaction.builder()
                .referenceNumber(UUID.randomUUID().toString())
                .requestId(requestId)
                .originAccount(originAccount)
                .toAccount(toAccount)
                .amount(amount)
                .fee(fee)
                .transactionType(transactionType)
                .status(TransactionStatus.SUCCESS)
                .description(description)
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction){
        BigDecimal totalDebited = transaction.getAmount().add(transaction.getFee());

        return  TransactionResponse.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .originAccountNumber(transaction.getOriginAccount() != null ? transaction.getOriginAccount()  .getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null)
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .totalDebited(totalDebited)
                .transactionType(transaction.getTransactionType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


}

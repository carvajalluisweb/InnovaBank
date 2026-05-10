package com.Innova.bank.transaction.mapper;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.transaction.dto.TransactionResponse;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionMapper {

    public Transaction toEntity(String requestId, String referenceNumber, Account originAccount, Account toAccount, BigDecimal amount,
            BigDecimal fee, TransactionType transactionType, TransactionStatus status, String description) {

        return Transaction.builder()
                .requestId(requestId)
                .referenceNumber(referenceNumber)
                .originAccount(originAccount)
                .toAccount(toAccount)
                .amount(amount)
                .fee(fee)
                .totalDebited(amount.add(fee))
                .transactionType(transactionType)
                .status(status)
                .description(description)
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());

        response.setReferenceNumber(transaction.getReferenceNumber());

        response.setOriginAccountNumber(transaction.getOriginAccount() != null ? transaction.getOriginAccount().getAccountNumber() : null);

        response.setDestinationAccountNumber(transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null);

        response.setOriginOwner(transaction.getOriginAccount() != null ? transaction.getOriginAccount().getUser().getEmail() : null);

        response.setDestinationOwner(transaction.getToAccount() != null ? transaction.getToAccount().getUser().getEmail() : null);

        response.setOriginAccountType(transaction.getOriginAccount() != null ? transaction.getOriginAccount().getAccountType().name(): null);

        response.setDestinationAccountType(transaction.getToAccount() != null ? transaction.getToAccount() .getAccountType().name() : null);

        response.setAmount(transaction.getAmount());

        response.setFee(transaction.getFee());

        response.setTotalDebited(transaction.getTotalDebited());

        response.setTransactionType(transaction.getTransactionType().name());

        response.setStatus(transaction.getStatus().name());

        response.setDescription(transaction.getDescription());

        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}
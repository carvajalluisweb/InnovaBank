package com.Innova.bank.fee.repository;

import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.fee.entity.TransactionFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionFeeRepository extends JpaRepository<com.Innova.bank.fee.entity.TransactionFee,Long> {

    Optional<TransactionFee> findByTransactionTypeAndActiveTrue(TransactionType transactionType);
}

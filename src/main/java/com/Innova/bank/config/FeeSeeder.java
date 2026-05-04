package com.Innova.bank.config;

import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.fee.entity.TransactionFee;
import com.Innova.bank.fee.repository.TransactionFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class FeeSeeder implements CommandLineRunner {

    private final TransactionFeeRepository repository;

    @Override
    public void run(String... args) {

        if(repository.count() > 0) return;

        repository.save(TransactionFee.builder()
                .transactionType(TransactionType.DEPOSIT)
                .feeAmount(new BigDecimal("0.00"))
                .active(true)
                .build());

        repository.save(TransactionFee.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .feeAmount(new BigDecimal("0.10"))
                .active(true)
                .build());

        repository.save(TransactionFee.builder()
                .transactionType(TransactionType.TRANSFER)
                .feeAmount(new BigDecimal("0.10"))
                .active(true)
                .build());
    }
}

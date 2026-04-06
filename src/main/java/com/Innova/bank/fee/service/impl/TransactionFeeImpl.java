package com.Innova.bank.fee.service.impl;

import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.fee.entity.TransactionFee;
import com.Innova.bank.fee.repository.TransactionFeeRepository;
import com.Innova.bank.fee.service.TransactionFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionFeeImpl implements TransactionFeeService {

    private final TransactionFeeRepository transactionFeeRepository;

    @Override
    public BigDecimal getFeeByTransactionType(TransactionType transactionType) {
        TransactionFee configFee = transactionFeeRepository.findByTransactionTypeAndActiveTrue(transactionType)
                .orElseThrow(() -> new ResourceNotFoundException("No existe configuracion activa de tarifa para el tipo de transaccion: " + transactionType.name()));

        return configFee.getFeeAmount();
    }
}

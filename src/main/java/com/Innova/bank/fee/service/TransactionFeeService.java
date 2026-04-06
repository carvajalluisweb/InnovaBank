package com.Innova.bank.fee.service;

import com.Innova.bank.enums.TransactionType;

import java.math.BigDecimal;

public interface TransactionFeeService {

    BigDecimal getFeeByTransactionType(TransactionType transactionType);
}

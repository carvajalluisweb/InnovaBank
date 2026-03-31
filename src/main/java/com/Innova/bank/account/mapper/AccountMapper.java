package com.Innova.bank.account.mapper;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.entity.Account;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.enums.AccountStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class AccountMapper {

    public Account toEntity(CreateAccountRequest request, User user){
        return Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();
    }

    public AccountResponse toResponse(Account account){
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .userId(account.getUser().getId())
                .userEmail(account.getUser().getEmail())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private String generateAccountNumber(){
        long number = 1000000000L + new Random().nextInt(900000000);
        return  String.valueOf(number);
    }

}

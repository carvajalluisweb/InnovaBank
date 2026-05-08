package com.Innova.bank.common.validation;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.account.repository.AccountRepository;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.enums.AccountStatus;
import com.Innova.bank.enums.AccountType;
import com.Innova.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountValidationService {

    private final ExceptionFactory exceptionFactory;
    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public void validateInitialBalance(BigDecimal balance) {

        if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {

            throw exceptionFactory.badRequest("El saldo inicial no puede ser negativo");
        }
    }

    public void validateUniqueAccountType(User user, AccountType accountType) {

        boolean exists = accountRepository.existsByUserAndAccountType(user, accountType);

        if (exists) {

            throw exceptionFactory.badRequest("El cliente ya tiene una cuenta " + accountType.name());
        }
    }

    public void validateAccountActive(AccountStatus status) {

        if (status != AccountStatus.ACTIVE) {

            throw exceptionFactory.badRequest("La cuenta no está activa");
        }
    }

    public void validateZeroBalance(BigDecimal balance) {

        if (balance.compareTo(BigDecimal.ZERO) > 0) {

            throw exceptionFactory.badRequest("La cuenta tiene saldo disponible");
        }
    }

    public String generateUniqueAccountNumber() {

        String accountNumber;

        do {

            accountNumber = accountNumberGenerator.generate();

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public void validateClosable(Account account) {

        if (
                account.getBalance().compareTo(BigDecimal.ZERO) > 0
        ) {

            throw exceptionFactory.badRequest("La cuenta tiene saldo disponible");
        }
    }
}
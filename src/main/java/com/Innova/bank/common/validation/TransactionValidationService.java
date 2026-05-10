package com.Innova.bank.common.validation;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.account.repository.AccountRepository;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.enums.*;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.transaction.repository.TransactionRepository;
import com.Innova.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionValidationService {

    @Value("${transaction.daily-limit}")
    private BigDecimal dailyLimit;

    private final ExceptionFactory exceptionFactory;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Transaction validateRequestId(String requestId) {

        if (requestId == null || requestId.isBlank()) {

            throw exceptionFactory.badRequest("El header X-Request-Id es obligatorio");
        }

        return transactionRepository.findByRequestId(requestId).orElse(null);
    }

    public Account[] lockAccountsForTransfer(String originAccountNumber, String toAccountNumber) {

        validateDifferentAccounts(originAccountNumber, toAccountNumber);

        if (originAccountNumber.compareTo(toAccountNumber) < 0) {

            Account first = findAccountByNumberForUpdate(originAccountNumber);

            Account second = findAccountByNumberForUpdate(toAccountNumber);

            return new Account[]{first, second};

        } else {

            Account second = findAccountByNumberForUpdate(toAccountNumber);

            Account first = findAccountByNumberForUpdate(originAccountNumber);

            return new Account[]{first, second};
        }
    }

    public void validateTransfer(User authenticatedUser, Account originAccount, Account toAccount, BigDecimal amount, BigDecimal fee) {

        validateActiveUser(authenticatedUser);

        validatePositiveAmount(amount);

        validateAccountActive(originAccount, "La cuenta origen no está activa");

        validateAccountActive(toAccount, "La cuenta destino no está activa");

        validateOwnership(authenticatedUser, originAccount);

        BigDecimal totalDebited = amount.add(fee);

        validateSufficientBalance(originAccount, totalDebited);

        validateDailyLimit(originAccount, totalDebited);
    }

    public void validateWithdrawal(User authenticatedUser, Account account, BigDecimal amount, BigDecimal fee) {

        validateActiveUser(authenticatedUser);

        validatePositiveAmount(amount);

        validateAccountActive(account, "La cuenta no está activa");

        validateOwnership(authenticatedUser, account);

        BigDecimal totalDebited = amount.add(fee);

        validateSufficientBalance(account, totalDebited);

        validateDailyLimit(account, totalDebited);
    }

    public void validateDifferentAccounts(String originAccount, String destinationAccount) {

        if (originAccount.equals(destinationAccount)) {

            throw exceptionFactory.badRequest("La cuenta origen y destino no pueden ser la misma");
        }
    }

    public void validatePositiveAmount(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw exceptionFactory.badRequest("El monto debe ser mayor a 0");
        }
    }

    public void validateOwnership(User authenticatedUser, Account account) {

        if (authenticatedUser.getRole() == Rol.ROLE_USER && !account.getUser().getId().equals(authenticatedUser.getId())) {

            throw exceptionFactory.badRequest("La cuenta no pertenece al usuario autenticado");
        }
    }

    public void validateSufficientBalance(Account account, BigDecimal totalDebited) {

        if (account.getBalance().compareTo(totalDebited) < 0) {

            throw exceptionFactory.badRequest("Saldo insuficiente para cubrir monto y fee");
        }
    }

    public void validateDailyLimit(Account originAccount, BigDecimal totalDebited) {

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();

        LocalDateTime end = start.plusDays(1).minusNanos(1);

        BigDecimal dailyTransferred = transactionRepository.sumDailyOutgoingWithFee(originAccount,
                        TransactionStatus.SUCCESS, List.of(TransactionType.TRANSFER, TransactionType.WITHDRAWAL),
                        start, end);

        BigDecimal projectedTotal = dailyTransferred.add(totalDebited);

        if (projectedTotal.compareTo(dailyLimit) > 0) {

            throw exceptionFactory.badRequest("La operación excede el límite diario permitido");
        }
    }

    public void validateAccountActive(Account account, String message) {

        if (account.getStatus() != AccountStatus.ACTIVE) {

            throw exceptionFactory.badRequest(message);
        }

        if (account.getUser().getStatus() != UserStatus.ACTIVE) {

            throw exceptionFactory.badRequest("El propietario de la cuenta no está activo");
        }
    }

    public void validateActiveUser(User user) {

        if (user.getStatus() != UserStatus.ACTIVE) {

            throw exceptionFactory.badRequest("El usuario no está activo");
        }
    }

    public Account findAccountByNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> exceptionFactory.notFound("Cuenta no encontrada con número: " + accountNumber));
    }

    public Account findAccountByNumberForUpdate(String accountNumber) {

        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> exceptionFactory.notFound("Cuenta no encontrada con número: "+ accountNumber));
    }

    public String generateTransactionReference() {

        Long sequence = transactionRepository.getNextTransactionSequence();

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        return String.format("TRX-%s-%06d", date, sequence);
    }
}
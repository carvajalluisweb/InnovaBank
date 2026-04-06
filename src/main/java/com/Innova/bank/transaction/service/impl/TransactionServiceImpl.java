package com.Innova.bank.transaction.service.impl;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.account.repository.AccountRepository;
import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.*;
import com.Innova.bank.fee.service.TransactionFeeService;
import com.Innova.bank.transaction.dto.CreateDepositRequest;
import com.Innova.bank.transaction.dto.CreateTransferRequest;
import com.Innova.bank.transaction.dto.CreateWithdrawalRequest;
import com.Innova.bank.transaction.dto.TransactionResponse;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.transaction.mapper.TransactionMapper;
import com.Innova.bank.transaction.repository.TransactionRepository;
import com.Innova.bank.transaction.service.TransactionService;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Value("${transaction.daily-limit}")
    private BigDecimal dailyLimit;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final AuditLogService auditLogService;
    private final TransactionFeeService transactionFeeService;

    @Override
    public TransactionResponse createTransfer(
            String requestId,
            CreateTransferRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        Transaction existing = validateIdempotency(requestId);
        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        try {
            Account[] lockedAccounts = lockAccountsForTransfer(
                    request.getOriginAccountNumber(),
                    request.getToAccountNumber()
            );

            Account originAccount = lockedAccounts[0];
            Account toAccount = lockedAccounts[1];

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.TRANSFER);

            validateTransfer(request, authenticatedUser, originAccount, toAccount, fee);

            BigDecimal totalDebited = request.getAmount().add(fee);

            originAccount.setBalance(originAccount.getBalance().subtract(totalDebited));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            Transaction transaction = transactionMapper.toEntity(
                    requestId,
                    originAccount,
                    toAccount,
                    request.getAmount(),
                    fee,
                    TransactionType.TRANSFER,
                    request.getDescription()
            );

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditSuccess(
                    authenticatedUser,
                    AuditAction.CREATE_TRANSFER,
                    "Transferencia realizada de " + originAccount.getAccountNumber()
                            + " a " + toAccount.getAccountNumber()
                            + " por " + request.getAmount()
                            + " con tarifa de " + fee,
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception exception) {
            auditFailure(
                    authenticatedUser,
                    AuditAction.CREATE_TRANSFER,
                    "Error al realizar la transferencia: " + exception.getMessage(),
                    httpRequest
            );
            throw exception;
        }
    }

    @Override
    public TransactionResponse createDeposit(
            String requestId,
            CreateDepositRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        Transaction existing = validateIdempotency(requestId);
        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        try {
            validateUserRol(authenticatedUser);

            Account toAccount = findAccountByNumberForUpdate(request.getToAccountNumber());
            validateAccountActive(toAccount, "La cuenta destino no está activa");

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.DEPOSIT);

            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            Transaction transaction = transactionMapper.toEntity(
                    requestId,
                    null,
                    toAccount,
                    request.getAmount(),
                    fee,
                    TransactionType.DEPOSIT,
                    request.getDescription()
            );

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditSuccess(
                    authenticatedUser,
                    AuditAction.CREATE_DEPOSIT,
                    "Depósito realizado a la cuenta " + toAccount.getAccountNumber()
                            + " por " + request.getAmount()
                            + " tarifa " + fee,
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception exception) {
            auditFailure(
                    authenticatedUser,
                    AuditAction.CREATE_DEPOSIT,
                    "Error al realizar el depósito: " + exception.getMessage(),
                    httpRequest
            );
            throw exception;
        }
    }

    @Override
    public TransactionResponse createWithdrawal(
            String requestId,
            CreateWithdrawalRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        Transaction existing = validateIdempotency(requestId);
        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        try {
            Account account = findAccountByNumberForUpdate(request.getToAccountNumber());

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.WITHDRAWAL);

            validateWithdrawal(request, authenticatedUser, account, fee);

            BigDecimal totalDebited = request.getAmount().add(fee);
            account.setBalance(account.getBalance().subtract(totalDebited));

            Transaction transaction = transactionMapper.toEntity(
                    requestId,
                    account,
                    null,
                    request.getAmount(),
                    fee,
                    TransactionType.WITHDRAWAL,
                    request.getDescription()
            );

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditSuccess(
                    authenticatedUser,
                    AuditAction.CREATE_WITHDRAWAL,
                    "Retiro realizado desde la cuenta " + account.getAccountNumber()
                            + " por " + request.getAmount()
                            + " con fee " + fee,
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception exception) {
            auditFailure(
                    authenticatedUser,
                    AuditAction.CREATE_WITHDRAWAL,
                    "Error al realizar retiro: " + exception.getMessage(),
                    httpRequest
            );
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(int page, int size) {
        User authenticatedUser = getAuthenticatedUser();

        return transactionRepository.findMyTransactions(
                authenticatedUser,
                PageRequest.of(page, size)
        ).map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(String accountNumber, int page, int size) {
        User authenticatedUser = getAuthenticatedUser();
        Account account = findAccountByNumber(accountNumber);

        if (authenticatedUser.getRole() == Rol.ROLE_USER
                && !account.getUser().getId().equals(authenticatedUser.getId())) {
            throw new UnauthorizedException("No tienes permisos para ver los movimientos de esta cuenta");
        }

        return transactionRepository.findByOriginAccountOrToAccountOrderByCreatedAtDesc(
                account,
                account,
                PageRequest.of(page, size)
        ).map(transactionMapper::toResponse);
    }

    private Transaction validateIdempotency(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new BadRequestException("El header X-Request-Id es obligatorio");
        }

        return transactionRepository.findByRequestId(requestId).orElse(null);
    }

    private Account[] lockAccountsForTransfer(String originAccountNumber, String toAccountNumber) {
        if (originAccountNumber.equals(toAccountNumber)) {
            throw new BadRequestException("La cuenta origen y destino no pueden ser la misma");
        }

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

    private void validateTransfer(
            CreateTransferRequest request,
            User authenticatedUser,
            Account originAccount,
            Account toAccount,
            BigDecimal fee
    ) {
        if (authenticatedUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("El usuario no está activo para realizar transferencias");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor a 0");
        }

        validateAccountActive(originAccount, "La cuenta origen no está activa");
        validateAccountActive(toAccount, "La cuenta destino no está activa");

        if (authenticatedUser.getRole() == Rol.ROLE_USER
                && !originAccount.getUser().getId().equals(authenticatedUser.getId())) {
            throw new UnauthorizedException("No puedes transferir desde una cuenta que no te pertenece");
        }

        BigDecimal totalDebited = request.getAmount().add(fee);

        if (originAccount.getBalance().compareTo(totalDebited) < 0) {
            throw new BadRequestException("Saldo insuficiente para cubrir monto y fee");
        }

        validateDailyLimit(originAccount, totalDebited);
    }

    private void validateWithdrawal(
            CreateWithdrawalRequest request,
            User authenticatedUser,
            Account account,
            BigDecimal fee
    ) {
        if (authenticatedUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("El usuario no está activo para realizar retiros");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor a 0");
        }

        validateAccountActive(account, "La cuenta no está activa");

        if (authenticatedUser.getRole() == Rol.ROLE_USER
                && !account.getUser().getId().equals(authenticatedUser.getId())) {
            throw new UnauthorizedException("No puedes retirar desde una cuenta que no te pertenece");
        }

        BigDecimal totalDebited = request.getAmount().add(fee);

        if (account.getBalance().compareTo(totalDebited) < 0) {
            throw new BadRequestException("Saldo insuficiente para cubrir monto y fee");
        }

        validateDailyLimit(account, totalDebited);
    }

    private void validateDailyLimit(Account originAccount, BigDecimal totalDebited) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        BigDecimal dailyTransferred = transactionRepository.sumDailyOutgoingWithFee(
                originAccount,
                TransactionStatus.SUCCESS,
                List.of(TransactionType.TRANSFER, TransactionType.WITHDRAWAL),
                start,
                end
        );

        BigDecimal projectedTotal = dailyTransferred.add(totalDebited);

        if (projectedTotal.compareTo(dailyLimit) > 0) {
            throw new BadRequestException("La operación excede el límite diario permitido");
        }
    }

    private void validateAccountActive(Account account, String message) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(message);
        }

        if (account.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("El propietario de la cuenta no está activo");
        }
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cuenta no encontrada con número: " + accountNumber
                ));
    }

    private Account findAccountByNumberForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cuenta no encontrada con número: " + accountNumber
                ));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("El usuario no está activo");
        }

        return user;
    }

    private void validateUserRol(User user) {
        if (user.getRole() != Rol.ROLE_OPERATOR && user.getRole() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("No tienes permisos para realizar esta acción");
        }
    }

    private void auditSuccess(
            User user,
            AuditAction action,
            String description,
            HttpServletRequest httpRequest
    ) {
        auditLogService.save(
                user.getId(),
                user.getEmail(),
                action,
                description,
                AuditStatus.SUCCESS,
                httpRequest
        );
    }

    private void auditFailure(
            User user,
            AuditAction action,
            String description,
            HttpServletRequest httpRequest
    ) {
        auditLogService.save(
                user.getId(),
                user.getEmail(),
                action,
                description,
                AuditStatus.FAILED,
                httpRequest
        );
    }
}
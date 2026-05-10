package com.Innova.bank.transaction.service.impl;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.common.security.AuthorizationService;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.common.validation.TransactionValidationService;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.fee.service.TransactionFeeService;
import com.Innova.bank.transaction.dto.CreateDepositRequest;
import com.Innova.bank.transaction.dto.CreateTransferRequest;
import com.Innova.bank.transaction.dto.CreateWithdrawalRequest;
import com.Innova.bank.transaction.dto.TransactionResponse;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.transaction.mapper.TransactionMapper;
import com.Innova.bank.transaction.repository.TransactionRepository;
import com.Innova.bank.transaction.service.TransactionService;
import com.Innova.bank.transaction.service.TransactionStatusService;
import com.Innova.bank.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionFeeService transactionFeeService;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionValidationService transactionValidationService;
    private final TransactionStatusService transactionStatusService;
    private final AuditFacade auditFacade;

    @Override
    public TransactionResponse createTransfer(String requestId, CreateTransferRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        Transaction existing = transactionValidationService.validateRequestId(requestId);

        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        String referenceNumber = transactionValidationService.generateTransactionReference();

        Account originAccount = null;
        Account toAccount = null;

        try {

            Account[] lockedAccounts = transactionValidationService.lockAccountsForTransfer(
                    request.getOriginAccountNumber(),
                    request.getToAccountNumber()
            );

            originAccount = lockedAccounts[0];
            toAccount = lockedAccounts[1];

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.TRANSFER);

            transactionValidationService.validateTransfer(
                    authenticatedUser,
                    originAccount,
                    toAccount,
                    request.getAmount(),
                    fee
            );

            BigDecimal totalDebited = request.getAmount().add(fee);

            originAccount.setBalance(originAccount.getBalance().subtract(totalDebited));

            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            Transaction savedTransaction = transactionStatusService.saveSuccessTransaction(
                    requestId,
                    referenceNumber,
                    originAccount,
                    toAccount,
                    request.getAmount(),
                    fee,
                    TransactionType.TRANSFER,
                    request.getDescription()
            );

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_TRANSFER,
                    "Transferencia realizada | Origen: "
                            + originAccount.getAccountNumber()
                            + " | Destino: "
                            + toAccount.getAccountNumber()
                            + " | Monto: "
                            + request.getAmount(),
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception ex) {

            transactionStatusService.saveFailedTransaction(
                    requestId,
                    referenceNumber,
                    originAccount,
                    toAccount,
                    request.getAmount(),
                    TransactionType.TRANSFER,
                    ex.getMessage()
            );

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_TRANSFER,
                    "Error transferencia | Origen: "
                            + request.getOriginAccountNumber()
                            + " | Destino: "
                            + request.getToAccountNumber()
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    public TransactionResponse createDeposit(String requestId, CreateDepositRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        Transaction existing = transactionValidationService.validateRequestId(requestId);

        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        String referenceNumber = transactionValidationService.generateTransactionReference();

        Account toAccount = null;

        try {

            authorizationService.requireOperatorOrAdmin(authenticatedUser);

            toAccount = transactionValidationService.findAccountByNumberForUpdate(request.getToAccountNumber());

            transactionValidationService.validateAccountActive(toAccount, "La cuenta destino no está activa");

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.DEPOSIT);

            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            Transaction savedTransaction = transactionStatusService.saveSuccessTransaction(
                    requestId,
                    referenceNumber,
                    null,
                    toAccount,
                    request.getAmount(),
                    fee,
                    TransactionType.DEPOSIT,
                    request.getDescription()
            );

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_DEPOSIT,
                    "Depósito realizado | Cuenta: "
                            + toAccount.getAccountNumber()
                            + " | Monto: "
                            + request.getAmount(),
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception ex) {

            transactionStatusService.saveFailedTransaction(
                    requestId,
                    referenceNumber,
                    null,
                    toAccount,
                    request.getAmount(),
                    TransactionType.DEPOSIT,
                    ex.getMessage()
            );

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_DEPOSIT,
                    "Error depósito | Cuenta: "
                            + request.getToAccountNumber()
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    public TransactionResponse createWithdrawal(String requestId, CreateWithdrawalRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        Transaction existing = transactionValidationService.validateRequestId(requestId);

        if (existing != null) {
            return transactionMapper.toResponse(existing);
        }

        String referenceNumber = transactionValidationService.generateTransactionReference();

        Account account = null;

        try {

            account = transactionValidationService.findAccountByNumberForUpdate(request.getAccountNumber());

            BigDecimal fee = transactionFeeService.getFeeByTransactionType(TransactionType.WITHDRAWAL);

            transactionValidationService.validateWithdrawal(authenticatedUser, account, request.getAmount(), fee);

            BigDecimal totalDebited = request.getAmount().add(fee);

            account.setBalance(account.getBalance().subtract(totalDebited));

            Transaction savedTransaction = transactionStatusService.saveSuccessTransaction(
                    requestId,
                    referenceNumber,
                    account,
                    null,
                    request.getAmount(),
                    fee,
                    TransactionType.WITHDRAWAL,
                    request.getDescription()
            );

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_WITHDRAWAL,
                    "Retiro realizado | Cuenta: "
                            + account.getAccountNumber()
                            + " | Monto: "
                            + request.getAmount(),
                    httpRequest
            );

            return transactionMapper.toResponse(savedTransaction);

        } catch (Exception ex) {

            transactionStatusService.saveFailedTransaction(
                    requestId,
                    referenceNumber,
                    account,
                    null,
                    request.getAmount(),
                    TransactionType.WITHDRAWAL,
                    ex.getMessage()
            );

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_WITHDRAWAL,
                    "Error retiro | Cuenta: "
                            + request.getAccountNumber()
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    public TransactionResponse reverseTransaction(String referenceNumber, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireOperatorOrAdmin(authenticatedUser);

            Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                            .orElseThrow(() -> exceptionFactory.notFound("Transacción no encontrada"));

            transactionStatusService.validateReversible(transaction);

            if (transaction.getTransactionType() == TransactionType.TRANSFER) {

                Account originAccount = transactionValidationService.findAccountByNumberForUpdate(transaction.getOriginAccount().getAccountNumber());

                Account destinationAccount = transactionValidationService.findAccountByNumberForUpdate(transaction.getToAccount().getAccountNumber());

                originAccount.setBalance(originAccount.getBalance().add(transaction.getTotalDebited()));

                destinationAccount.setBalance(destinationAccount.getBalance().subtract(transaction.getAmount()));
            }

            if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {

                Account originAccount = transactionValidationService.findAccountByNumberForUpdate(transaction.getOriginAccount().getAccountNumber());

                originAccount.setBalance(originAccount.getBalance().add(transaction.getTotalDebited()));
            }

            Transaction reversedTransaction = transactionStatusService.reverseTransaction(transaction);

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.REVERSE_TRANSACTION,
                    "Transacción reversada | Referencia: "
                            + transaction.getReferenceNumber()
                            + " | Tipo: "
                            + transaction.getTransactionType().name(),
                    httpRequest
            );

            return transactionMapper.toResponse(
                    reversedTransaction
            );

        } catch (Exception ex) {

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.REVERSE_TRANSACTION,
                    "Error reversando transacción | Referencia: "
                            + referenceNumber
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(int page, int size) {

        User authenticatedUser = currentUserService.getCurrentUser();

        return transactionRepository.findMyTransactions(
                        authenticatedUser,
                        PageRequest.of(page, size)
                )
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(String accountNumber, int page, int size) {

        User authenticatedUser = currentUserService.getCurrentUser();

        Account account = transactionValidationService.findAccountByNumber(accountNumber);

        transactionValidationService.validateOwnership(
                authenticatedUser,
                account
        );

        return transactionRepository.findByOriginAccountOrToAccountOrderByCreatedAtDesc(
                        account,
                        account,
                        PageRequest.of(page, size)
                )
                .map(transactionMapper::toResponse);
    }
}
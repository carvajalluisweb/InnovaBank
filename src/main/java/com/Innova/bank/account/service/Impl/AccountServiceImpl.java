package com.Innova.bank.account.service.Impl;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.dto.UpdateAccountStatusRequest;
import com.Innova.bank.account.entity.Account;
import com.Innova.bank.account.mapper.AccountMapper;
import com.Innova.bank.account.repository.AccountRepository;
import com.Innova.bank.account.service.AccountService;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.constant.MessageConstants;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.common.security.AuthorizationService;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.common.validation.AccountValidationService;
import com.Innova.bank.common.validation.UserValidationService;
import com.Innova.bank.enums.AccountStatus;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.repository.UserCustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final AccountMapper accountMapper;

    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    private final UserValidationService userValidationService;
    private final AccountValidationService accountValidationService;

    private final AuditFacade auditFacade;
    private final ExceptionFactory exceptionFactory;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {

        User authenticatedUser = currentUserService.getCurrentUser();

        userValidationService.validateActive(authenticatedUser);

        return accountRepository
                .findByUser(authenticatedUser)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMyAccountByAccountNumber(String accountNumber) {

        User authenticatedUser = currentUserService.getCurrentUser();

        userValidationService.validateActive(authenticatedUser);

        Account account = accountRepository.findByAccountNumberAndUser(accountNumber, authenticatedUser)
                        .orElseThrow(() -> exceptionFactory.notFound("Cuenta no encontrada"));

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {

        User authenticatedUser = currentUserService.getCurrentUser();

        authorizationService.requireOperatorOrAdmin(authenticatedUser);

        return accountRepository.findAll().stream().map(accountMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {

        User authenticatedUser = currentUserService.getCurrentUser();

        authorizationService.requireOperatorOrAdmin(authenticatedUser);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> exceptionFactory.notFound("Cuenta no encontrada"));

        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireOperatorOrAdmin(authenticatedUser);

            UserCustomer customer = userCustomerRepository.findByIdNumber(request.getUserIdNumber())
                            .orElseThrow(() -> exceptionFactory.notFound(MessageConstants.NOT_FOUND_CLIENT));

            User targetUser = customer.getUser();

            userValidationService.validateActive(targetUser);

            accountValidationService.validateInitialBalance(request.getInitialBalance());

            accountValidationService.validateUniqueAccountType(targetUser, request.getAccountType());

            String accountNumber = accountValidationService.generateUniqueAccountNumber();

            Account account = accountMapper.toEntity(request, targetUser, accountNumber);

            Account savedAccount = accountRepository.save(account);

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_ACCOUNT,
                    "Cuenta creada | Cuenta: "
                            + savedAccount.getAccountNumber()
                            + " | Cliente: "
                            + customer.getIdNumber(),
                    httpRequest
            );

            return accountMapper.toResponse(savedAccount);

        } catch (Exception ex) {

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_ACCOUNT,
                    "Error creando cuenta | Cliente: "
                            + request.getUserIdNumber()
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    public AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireAdmin(authenticatedUser);

            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> exceptionFactory.notFound("Cuenta no encontrada"));

            if (account.getStatus() == request.getStatus()) {

                throw exceptionFactory.badRequest("La cuenta ya tiene ese estado");
            }

            if (account.getStatus() == AccountStatus.CLOSED) {

                throw exceptionFactory.badRequest("No se puede modificar una cuenta cerrada");
            }

            if (request.getStatus() == AccountStatus.CLOSED) {

                accountValidationService.validateClosable(account);
            }

            account.setStatus(request.getStatus());

            Account updatedAccount = accountRepository.save(account);

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ACCOUNT_STATUS,
                    "Estado cuenta actualizado | Cuenta: "
                            + account.getAccountNumber()
                            + " | Estado: "
                            + request.getStatus().name(),
                    httpRequest
            );

            return accountMapper.toResponse(updatedAccount);

        } catch (Exception ex) {

            auditFacade.failed(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ACCOUNT_STATUS,
                    "Error actualizando estado cuenta | Cuenta: "
                            + accountNumber
                            + " | Error: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }
}
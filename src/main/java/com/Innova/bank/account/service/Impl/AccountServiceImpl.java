package com.Innova.bank.account.service.Impl;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.dto.UpdateAccountStatusRequest;
import com.Innova.bank.account.entity.Account;
import com.Innova.bank.account.mapper.AccountMapper;
import com.Innova.bank.account.repository.AccountRepository;
import com.Innova.bank.account.service.AccountService;
import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.*;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final AuditLogService auditLogService;


    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User authenticatedUser = getAuthenticatedUser();

        return accountRepository.findByUser(authenticatedUser)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMyAccountByAccountNumber(String accountNumber) {
        User authenticatedUser = getAuthenticatedUser();

        Account account = accountRepository.findByAccountNumberAndUser(accountNumber, authenticatedUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        User authenticatedUser = getAuthenticatedUser();
        validateRolUser(authenticatedUser);

        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        User authenticatedUser = getAuthenticatedUser();
        validateRolUser(authenticatedUser);

        Account account = findAccountByAccountNumber(accountNumber);
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request, HttpServletRequest httpRequest) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            validateRolUser(authenticatedUser);

            User targetUser = userRepository.findByIdNumber(request.getUserIdNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + request.getUserIdNumber()));

            if (targetUser.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("El usuario no esta activo");
            }
            Account account = accountMapper.toEntity(request, targetUser);
            Account savedAccount = accountRepository.save(account);

            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_ACCOUNT,
                    "Cuenta creada para usuario" + targetUser.getFirstName() + targetUser.getLastName(),
                    AuditStatus.SUCCESS,
                    httpRequest
            );

            return accountMapper.toResponse(savedAccount);

        } catch (Exception exception) {
            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.CREATE_ACCOUNT,
                    "Error al crear cuenta: " + exception.getMessage(),
                    AuditStatus.FAILED,
                    httpRequest
            );
            throw exception;
        }
    }

    @Override
    public AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request, HttpServletRequest httpRequest) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            validateAdmin(authenticatedUser);

            Account account = findAccountByAccountNumber(accountNumber);

            if (account.getStatus() == request.getStatus()) {
                throw new BadRequestException("La cuentea ya tiene ese estado");
            }

            if (account.getStatus() == AccountStatus.CLOSED) {
                throw new BadRequestException("No se puede modificar una cuenta cerrada;");
            }

            account.setStatus(request.getStatus());
            Account updateAccount = accountRepository.save(account);

            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ACCOUNT_STATUS,
                    "Estado de cuanta actualizado a " + request.getStatus().name(),
                    AuditStatus.SUCCESS,
                    httpRequest
            );

            return accountMapper.toResponse(updateAccount);

        } catch (Exception exception) {
            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ACCOUNT_STATUS,
                    "Error al Actualizar estado de cuenta: " + exception.getMessage(),
                    AuditStatus.FAILED,
                    httpRequest
            );
            throw exception;
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Account findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Numero de Cuenta" + accountNumber + "No encontrada"));
    }

    private void validateAdmin(User user) {
        if (user.getRole() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("No tiene permisos para realizar esta accion");
        }
    }

    private void validateRolUser(User user) {
        if (user.getRole() != Rol.ROLE_OPERATOR && user.getRole() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("No tiene permiso para realizar esta accion");
        }
    }
}

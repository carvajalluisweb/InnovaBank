package com.Innova.bank.account.controller;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.dto.UpdateAccountStatusRequest;
import com.Innova.bank.account.service.AccountService;
import com.Innova.bank.common.response.ApiResponse;
import com.Innova.bank.common.response.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ResponseFactory responseFactory;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts() {

        return responseFactory.ok("Cuentas obtenidas correctamente", accountService.getMyAccounts());
    }

    @GetMapping("/me/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccountByAccountNumber(@PathVariable String accountNumber) {

        return responseFactory.ok("Cuenta obtenida correctamente", accountService.getMyAccountByAccountNumber(accountNumber));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {

        return responseFactory.ok("Cuentas obtenidas correctamente", accountService.getAllAccounts());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNumber(@PathVariable String accountNumber) {

        return responseFactory.ok("Cuenta obtenida correctamente", accountService.getAccountByAccountNumber(accountNumber));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request, HttpServletRequest httpRequest) {

        return responseFactory.ok("Cuenta creada correctamente", accountService.createAccount(request, httpRequest));
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(@PathVariable String accountNumber, @Valid @RequestBody UpdateAccountStatusRequest request, HttpServletRequest httpRequest) {

        return responseFactory.ok("Estado de cuenta actualizado correctamente", accountService.updateAccountStatus(accountNumber, request, httpRequest));
    }
}
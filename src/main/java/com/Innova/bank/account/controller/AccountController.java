package com.Innova.bank.account.controller;

import com.Innova.bank.account.dto.AccountResponse;
import com.Innova.bank.account.dto.CreateAccountRequest;
import com.Innova.bank.account.dto.UpdateAccountStatusRequest;
import com.Innova.bank.account.service.AccountService;
import com.Innova.bank.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(){
        List<AccountResponse> response = accountService.getMyAccounts();

        return ResponseEntity.ok(
                ApiResponse.<List<AccountResponse>>builder()
                        .success(true)
                        .message("Cuentas del usuario obtenidas correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/me/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccountByAccounNumber(@PathVariable String accountNumber){
        AccountResponse response = accountService.getMyAccountByAccountNumber(accountNumber);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Cuenta del usuario obtenida correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/allAccounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(){
        List<AccountResponse> response = accountService.getAllAccounts();

        return ResponseEntity.ok(
                ApiResponse.<List<AccountResponse>>builder()
                        .success(true)
                        .message("Cuentas obtenidas correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNumber(@PathVariable String accountNumber){
        AccountResponse response = accountService.getAccountByAccountNumber(accountNumber);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Cuenta obtenida correctamente")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/createAccount")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request, HttpServletRequest httpRequest){
        AccountResponse response = accountService.createAccount(request,httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Cuenta creada correctamente")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(@PathVariable String accountNumber, @Valid @RequestBody UpdateAccountStatusRequest request, HttpServletRequest httpRequest){
        AccountResponse response = accountService.updateAccountStatus(accountNumber,request,httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponse>builder()
                        .success(true)
                        .message("Estado de cuenta actualizado correctamente")
                        .data(response)
                        .build()
        );
    }

}

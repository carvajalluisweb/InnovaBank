package com.Innova.bank.transaction.controller;

import com.Innova.bank.common.response.ApiResponse;
import com.Innova.bank.transaction.dto.CreateDepositRequest;
import com.Innova.bank.transaction.dto.CreateTransferRequest;
import com.Innova.bank.transaction.dto.CreateWithdrawalRequest;
import com.Innova.bank.transaction.dto.TransactionResponse;
import com.Innova.bank.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransfer(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateTransferRequest request,
            HttpServletRequest httpRequest
    ) {
        TransactionResponse response = transactionService.createTransfer(requestId, request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .success(true)
                        .message("Transferencia realizada correctamente")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> createDeposit(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateDepositRequest request,
            HttpServletRequest httpRequest
    ) {
        TransactionResponse response = transactionService.createDeposit(requestId, request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .success(true)
                        .message("Depósito realizado correctamente")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<TransactionResponse>> createWithdrawal(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateWithdrawalRequest request,
            HttpServletRequest httpRequest
    ) {
        TransactionResponse response = transactionService.createWithdrawal(requestId, request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .success(true)
                        .message("Retiro realizado correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TransactionResponse> response = transactionService.getMyTransactions(page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionResponse>>builder()
                        .success(true)
                        .message("Movimientos obtenidos correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TransactionResponse> response = transactionService.getAccountTransactions(accountNumber, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionResponse>>builder()
                        .success(true)
                        .message("Movimientos de cuenta obtenidos correctamente")
                        .data(response)
                        .build()
        );
    }
}
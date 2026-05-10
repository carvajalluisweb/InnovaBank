package com.Innova.bank.transaction.controller;

import com.Innova.bank.common.response.ApiResponse;
import com.Innova.bank.common.response.ResponseFactory;
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
    private final ResponseFactory responseFactory;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransfer(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateTransferRequest request,
            HttpServletRequest httpRequest
    ) {

        TransactionResponse response = transactionService.createTransfer(requestId, request, httpRequest);

        return responseFactory.ok("Transferencia realizada correctamente", response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> createDeposit(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateDepositRequest request,
            HttpServletRequest httpRequest
    ) {

        TransactionResponse response = transactionService.createDeposit(requestId, request, httpRequest);

        return responseFactory.ok("Depósito realizado correctamente", response);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<TransactionResponse>> createWithdrawal(
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateWithdrawalRequest request,
            HttpServletRequest httpRequest
    ) {

        TransactionResponse response = transactionService.createWithdrawal(requestId, request, httpRequest);

        return responseFactory.ok("Retiro realizado correctamente", response);
    }

    @PostMapping("/{referenceNumber}/reverse")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @PathVariable String referenceNumber,
            HttpServletRequest httpRequest
    ) {

        TransactionResponse response = transactionService.reverseTransaction(referenceNumber, httpRequest);

        return responseFactory.ok("Transacción reversada correctamente", response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<TransactionResponse> response = transactionService.getMyTransactions(page, size);

        return responseFactory.ok("Movimientos obtenidos correctamente", response);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<TransactionResponse> response = transactionService.getAccountTransactions(accountNumber, page, size);

        return responseFactory.ok("Movimientos de cuenta obtenidos correctamente", response);
    }

}
package com.Innova.bank.transaction.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private String originAccountNumber;
    private String destinationAccountNumber;
    private String originOwner;
    private String destinationOwner;
    private String originAccountType;
    private String destinationAccountType;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal totalDebited;
    private String transactionType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}

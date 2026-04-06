package com.Innova.bank.fee.entity;

import com.Innova.bank.enums.TransactionType;
import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_fee_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, unique = true, length = 20)
    private TransactionType transactionType;

    @Column(name = "fee_amount", nullable = false, precision = 15, scale=2)
    private BigDecimal feeAmount;

    @Column(nullable = false)
    private boolean active;
}

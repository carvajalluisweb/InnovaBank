package com.Innova.bank.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransferRequest {

    @NotBlank(message = "La cuenta origen es obligatoria")
    private String originAccountNumber;

    @NotBlank(message = "La cuenta destino es obligatoria")
    private String toAccountNumber;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayo a 0")
    private BigDecimal amount;

    private String description;
}

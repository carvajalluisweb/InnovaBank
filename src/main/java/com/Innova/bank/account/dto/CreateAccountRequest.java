package com.Innova.bank.account.dto;

import com.Innova.bank.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull(message = "La cédula del usuario es obligatorio")
    private String userIdNumber;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType accountType;

    private BigDecimal initialBalance;
}

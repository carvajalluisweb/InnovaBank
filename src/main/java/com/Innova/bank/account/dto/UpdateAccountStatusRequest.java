package com.Innova.bank.account.dto;

import com.Innova.bank.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private AccountStatus status;
}

package com.Innova.bank.user.dto;

import com.Innova.bank.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private UserStatus status;
}
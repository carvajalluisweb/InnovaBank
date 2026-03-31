package com.Innova.bank.user.dto;

import com.Innova.bank.enums.Rol;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRoleRequest {

    @NotNull(message = "El rol es obligatorio")
    private Rol role;
}
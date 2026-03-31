package com.Innova.bank.user.dto;

import com.Innova.bank.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMyProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El número de teléfono es obligatorio")
    private String phoneNumber;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 1, message = "La edad debe ser mayor a 0")
    private Integer age;

    @NotNull(message = "El sexo es obligatorio")
    private Gender gender;
}
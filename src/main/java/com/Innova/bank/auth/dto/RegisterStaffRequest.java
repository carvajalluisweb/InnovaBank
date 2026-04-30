package com.Innova.bank.auth.dto;

import com.Innova.bank.enums.Gender;
import com.Innova.bank.enums.Position;
import com.Innova.bank.enums.Rol;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterStaffRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 10, max = 10, message = "El número de teléfono debe tener 10 dígitos")
    private String phoneNumber;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 18, message = "Debe ser mayor de edad")
    private Integer age;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    private String idNumber;

    @NotNull(message = "El rol es obligatorio")
    private Rol role;

    @NotNull(message = "Posición obligatoria")
    private Position position;
}

package com.Innova.bank.common.audit;

import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class AuditMessageFactory {

    public String customerRegistered(String email) {
        return "Cliente registrado: " + email;
    }

    public String customerRegisterFailed(String email,Exception ex) {
        return "Error al registrar cliente: " + ex.getMessage() + ": " + email;
    }

    public String staffRegistered(String email) {
        return "Usuario interno registrado: " + email;
    }

    public String staffRegisterFailed(Exception ex) {
        return "Error al registrar usuario interno: " + ex.getMessage();
    }

    public String loginSuccess() {
        return "Inicio de sesión exitoso";
    }

    public String logoutSuccess() {
        return "Cierre de sesión exitoso";
    }

    public String failedLogin() {
        return "Intento de inicio de sesión fallido";
    }

    public String refreshSuspicious() {
        return "Intento de reutilización o manipulación de refresh token";
    }

    public String roleUpdated(String email, Rol role) {
        return "Rol actualizado a " + role.name() + " usuario: " + email;
    }

    public String statusUpdated(String email, UserStatus status) {
        return "Estado actualizado a " + status.name() + " usuario: " + email;
    }

    public String error(String prefix, Exception ex) {
        return prefix + ex.getMessage();
    }
}
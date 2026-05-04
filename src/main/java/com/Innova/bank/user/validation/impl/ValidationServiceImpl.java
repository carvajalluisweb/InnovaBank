package com.Innova.bank.user.validation.impl;

import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.user.validation.ValidationService;
import com.Innova.bank.enums.Position;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final ExceptionFactory exceptionFactory;

    @Override
    public void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw exceptionFactory.badRequest("Las contraseñas no son iguales");
        }
    }

    @Override
    public void validateInternalRole(Rol role) {
        if (role != Rol.ROLE_ADMIN && role != Rol.ROLE_OPERATOR) {
            throw exceptionFactory.badRequest("Solo se permite rol de ADMIN o OPERATOR");
        }
    }

    @Override
    public void validateRolePosition(Rol role, Position position) {

        if (role == Rol.ROLE_ADMIN &&
                position != Position.GENERAL_MANAGER) {

            throw exceptionFactory.badRequest("ROLE_ADMIN solo puede ser GENERAL_MANAGER");
        }

        if (role == Rol.ROLE_OPERATOR && position != Position.TELLER && position != Position.CUSTOMER_SERVICE) {

            throw exceptionFactory.badRequest( "ROLE_OPERATOR solo puede ser TELLER o CUSTOMER_SERVICE");
        }
    }

    @Override
    public void validateRoleChange(User targetUser, UpdateUserRoleRequest request) {

        Rol currentRole = targetUser.getRole();
        Rol newRole = request.getRole();

        if (currentRole == newRole) {
            throw exceptionFactory.badRequest( "El usuario ya tiene ese rol");
        }

        boolean currentIsStaff = currentRole == Rol.ROLE_ADMIN || currentRole == Rol.ROLE_OPERATOR;

        boolean newIsStaff = newRole == Rol.ROLE_ADMIN || newRole == Rol.ROLE_OPERATOR;

        if (!currentIsStaff || !newIsStaff) {
            throw exceptionFactory.badRequest( "Solo se permite cambio entre ADMIN y OPERATOR");
        }
    }

    @Override
    public void validateStatusChange(User targetUser, UpdateUserStatusRequest request) {
        if (targetUser.getStatus() == request.getStatus()) {
            throw exceptionFactory.badRequest( "El usuario ya tiene ese estado" );
        }
    }
}
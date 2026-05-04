package com.Innova.bank.common.security;

import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.Innova.bank.common.constant.MessageConstants.ACCESS_DENIED;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ExceptionFactory exceptionFactory;

    public void requireAdmin(User user) {
        if (user.getRole() != Rol.ROLE_ADMIN) {
            throw exceptionFactory.unauthorized(ACCESS_DENIED);
        }
    }

    public void requireOperatorOrAdmin(User user) {
        if (user.getRole() != Rol.ROLE_ADMIN && user.getRole() != Rol.ROLE_OPERATOR) {
            throw exceptionFactory.unauthorized(ACCESS_DENIED);
        }
    }

    public void requireNotSelf(User currentUser, Long targetUserId, String message) {
        if (currentUser.getId().equals(targetUserId)) {
            throw exceptionFactory.badRequest(message);
        }
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Rol.ROLE_ADMIN;
    }

    public boolean isOperator(User user) {
        return user.getRole() == Rol.ROLE_OPERATOR;
    }

    public boolean isStaff(User user) {
        return isAdmin(user) || isOperator(user);
    }
}
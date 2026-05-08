package com.Innova.bank.common.validation;

import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final ExceptionFactory exceptionFactory;

    public void validateActive(User user) {

        if (user.getStatus() != UserStatus.ACTIVE) {

            throw exceptionFactory.badRequest("El usuario no está activo");
        }
    }
}
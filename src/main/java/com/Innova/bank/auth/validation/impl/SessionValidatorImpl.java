package com.Innova.bank.auth.validation.impl;

import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.validation.SessionValidator;
import com.Innova.bank.common.constant.MessageConstants;
import com.Innova.bank.common.exception.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionValidatorImpl implements SessionValidator {

    private final ExceptionFactory exceptionFactory;

    @Override
    public void validateActive(SessionToken session) {
        if (!session.isActive()) {
            throw exceptionFactory.unauthorized("La sesión ya no está activa");
        }
    }

    @Override
    public void validateRefreshNotExpired(SessionToken session) {
        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            throw exceptionFactory.unauthorized(MessageConstants.REFRESH_TOKEN_EXPIRED);
        }
    }

    @Override
    public void validateUsableForRefresh(SessionToken session) {
        validateActive(session);
        validateRefreshNotExpired(session);
    }
}
package com.Innova.bank.common.security;

import com.Innova.bank.common.constant.SecurityConstants;
import com.Innova.bank.common.exception.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.Innova.bank.common.constant.MessageConstants.INVALID_TOKEN_FORMAT;
import static com.Innova.bank.common.constant.MessageConstants.TOKEN_NOT_PROVIDED;

@Component
@RequiredArgsConstructor
public class TokenExtractor {

    private final ExceptionFactory exceptionFactory;

    public String extract(String authHeader) {

        if (authHeader == null || authHeader.isBlank()) {
            throw exceptionFactory.badRequest(TOKEN_NOT_PROVIDED);
        }

        if (!authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw exceptionFactory.badRequest(INVALID_TOKEN_FORMAT);
        }

        return authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
    }
}
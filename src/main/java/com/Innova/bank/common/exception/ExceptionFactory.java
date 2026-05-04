package com.Innova.bank.common.exception;

import org.springframework.stereotype.Component;

@Component
public class ExceptionFactory {

    public BadRequestException badRequest(String message) {
        return new BadRequestException(message);
    }

    public ForbiddenException forbidden(String message) {
        return new ForbiddenException(message);
    }

    public UnauthorizedException unauthorized(String message) {
        return new UnauthorizedException(message);
    }

    public ResourceNotFoundException notFound(String message) {
        return new ResourceNotFoundException(message);
    }
}
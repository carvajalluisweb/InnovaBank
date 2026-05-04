package com.Innova.bank.common.exception;

import com.Innova.bank.common.response.ErrorResponse;
import com.Innova.bank.common.web.RequestContextService;
import com.Innova.bank.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.Innova.bank.common.constant.MessageConstants.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final RequestContextService requestContextService;

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorCode.BAD_REQUEST,
                null,
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorCode.RESOURCE_NOT_FOUND,
                null,
                request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                ErrorCode.UNAUTHORIZED,
                null,
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                ErrorCode.FORBIDDEN,
                null,
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                EMAIL_PASS_INVALID,
                ErrorCode.INVALID_CREDENTIALS,
                null,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validations = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validations.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR,
                validations,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {

        ex.printStackTrace();

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR,
                null,
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, ErrorCode code, Map<String, String> errors, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(message)
                .code(code)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(requestContextService.getPath(request))
                .requestId(requestContextService.getRequestId(request))
                .errors(errors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
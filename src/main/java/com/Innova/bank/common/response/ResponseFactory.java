package com.Innova.bank.common.response;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

    public <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {

        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .data(data)
                        .build()
        );
    }

    public ResponseEntity<ApiResponse<Void>> ok(String message) {

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
}
package com.Innova.bank.common.response;

import com.Innova.bank.enums.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private boolean success;
    private String message;
    private ErrorCode code;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private String requestId;
    private Map<String, String> errors;
}
package com.Innova.bank.audit.mapper;

import com.Innova.bank.audit.entity.AuditLog;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(
            Long userId,
            String email,
            AuditAction action,
            String description,
            AuditStatus status,
            String ipAddress,
            String endpoint,
            String httpMethod,
            String userAgent,
            String requestId
    ) {
        return AuditLog.builder()
                .userId(userId)
                .email(email)
                .action(action)
                .description(description)
                .status(status)
                .ipAddress(ipAddress)
                .endpoint(endpoint)
                .httpMethod(httpMethod)
                .userAgent(userAgent)
                .requestId(requestId)
                .build();
    }
}
package com.Innova.bank.audit.mapper;

import com.Innova.bank.audit.entity.AuditLog;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(
            Long userId,
            String email,
            AuditAction action,
            String description,
            AuditStatus status,
            String ipAddress
    ) {
        return AuditLog.builder()
                .userId(userId)
                .email(email)
                .action(action)
                .description(description)
                .ipAddress(ipAddress)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
package com.Innova.bank.audit.service;

import com.Innova.bank.audit.entity.AuditLog;
import com.Innova.bank.audit.mapper.AuditLogMapper;
import com.Innova.bank.audit.repository.AuditLogRepository;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public void save(
            Long userId,
            String email,
            AuditAction action,
            String description,
            AuditStatus status,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIp(request);

        AuditLog auditLog = auditLogMapper.toEntity(
                userId,
                email,
                action,
                description,
                status,
                ipAddress
        );

        auditLogRepository.save(auditLog);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
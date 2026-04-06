package com.Innova.bank.audit.service.impl;

import com.Innova.bank.audit.entity.AuditLog;
import com.Innova.bank.audit.mapper.AuditLogMapper;
import com.Innova.bank.audit.repository.AuditLogRepository;
import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(
            Long userId,
            String email,
            AuditAction action,
            String description,
            AuditStatus status,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIp(request);
        String endpoint = getEndpoint(request);
        String httpMethod = getHttpMethod(request);
        String userAgent = getUserAgent(request);
        String requestId = getRequestId(request);

        AuditLog auditLog = auditLogMapper.toEntity(
                userId,
                email,
                action,
                description,
                status,
                ipAddress,
                endpoint,
                httpMethod,
                userAgent,
                requestId
        );

        auditLogRepository.save(auditLog);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String getEndpoint(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : null;
    }

    private String getHttpMethod(HttpServletRequest request) {
        return request != null ? request.getMethod() : null;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return UUID.randomUUID().toString();
        }

        Object requestId = request.getAttribute("requestId");
        if (requestId != null) {
            return requestId.toString();
        }

        return UUID.randomUUID().toString();
    }
}
package com.Innova.bank.common.audit;

import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditFacade {

    private final AuditLogService auditLogService;

    public void success(Long userId, String email, AuditAction action, String description, HttpServletRequest request) {
        save(userId, email, action, description, AuditStatus.SUCCESS, request);
    }

    public void failed(Long userId, String email, AuditAction action, String description, HttpServletRequest request ) {
        save(userId, email, action, description, AuditStatus.FAILED, request);
    }

    public void save(Long userId, String email, AuditAction action, String description, AuditStatus status, HttpServletRequest request) {
        auditLogService.save(userId, email, action, description, status, request);
    }
}
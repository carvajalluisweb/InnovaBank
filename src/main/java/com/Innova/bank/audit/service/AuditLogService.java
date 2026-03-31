package com.Innova.bank.audit.service;

import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

    void save(
            Long userId,
            String email,
            AuditAction action,
            String description,
            AuditStatus status,
            HttpServletRequest request
    );
}
package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.dto.AuthResponse;
import com.Innova.bank.auth.dto.LoginRequest;
import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.mapper.AuthMapper;
import com.Innova.bank.auth.service.AuthAttemptService;
import com.Innova.bank.auth.service.SessionAccessService;
import com.Innova.bank.auth.service.SessionService;
import com.Innova.bank.auth.service.TokenService;
import com.Innova.bank.auth.validation.SessionValidator;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.audit.AuditMessageFactory;
import com.Innova.bank.common.constant.ValidationConstants;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.common.exception.ForbiddenException;
import com.Innova.bank.common.security.TokenExtractor;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.service.UserFinderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.Innova.bank.common.constant.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionAccessServiceImpl implements SessionAccessService {

    private final AuthenticationManager authenticationManager;
    private final AuthAttemptService authAttemptService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final AuthMapper authMapper;
    private final AuditFacade auditFacade;
    private final ExceptionFactory exceptionFactory;
    private final UserFinderService userFinderService;
    private final TokenExtractor tokenExtractor;
    private final SessionValidator sessionValidator;
    private final AuditMessageFactory auditMessageFactory;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        User user = userFinderService.findByEmail(request.getEmail());

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException(USER_BLOCKED);
        }

        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        } catch (Exception ex) {

            authAttemptService.increaseFailedAttempts(user);

            User updatedUser = userFinderService.findById(user.getId());

            auditFacade.failed(user.getId(), user.getEmail(), AuditAction.FAILED_LOGIN,auditMessageFactory.failedLogin(), httpRequest);

            if (updatedUser.getStatus() == UserStatus.BLOCKED) {
                throw new ForbiddenException("Usuario bloqueado por múltiples intentos fallidos. Contacte al administrador");
            }

            int remainingAttempts = ValidationConstants.MAX_LOGIN_ATTEMPTS - updatedUser.getFailedAttempts();

            throw exceptionFactory.badRequest("Credenciales inválidas. Intentos restantes: " + remainingAttempts);
        }

        authAttemptService.resetFailedAttempts(user);

        sessionService.deactivateAllUserSessions(user);

        UserDetails userDetails = buildUserDetails(user);

        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenService.generateAccessToken(userDetails, sessionId);

        String refreshToken = tokenService.generateRefreshToken(userDetails, sessionId);

        sessionService.createSession(user, sessionId, accessToken, refreshToken );

        auditFacade.success(user.getId(), user.getEmail(), AuditAction.LOGIN, auditMessageFactory.loginSuccess(), httpRequest);

        return authMapper.toAuthResponse(accessToken, refreshToken, tokenService.getAccessExpiration(), tokenService.getRefreshExpiration());
    }

    @Override
    @Transactional
    public void logout(String authHeader, HttpServletRequest httpRequest) {

        String token = tokenExtractor.extract(authHeader);

        String sessionId = tokenService.extractSessionId(token);

        SessionToken sessionToken = sessionService.findActiveBySessionId(sessionId);

        sessionValidator.validateActive(sessionToken);

        sessionService.deactivateSession(sessionToken);

        auditFacade.success(sessionToken.getUser().getId(), sessionToken.getUser().getEmail(),
                AuditAction.LOGOUT, auditMessageFactory.logoutSuccess(), httpRequest);
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}
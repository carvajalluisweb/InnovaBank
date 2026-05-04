package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.dto.AuthResponse;
import com.Innova.bank.auth.dto.RefreshTokenRequest;
import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.mapper.AuthMapper;
import com.Innova.bank.auth.service.RefreshTokenService;
import com.Innova.bank.auth.service.SessionService;
import com.Innova.bank.auth.service.TokenService;
import com.Innova.bank.auth.validation.SessionValidator;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.audit.AuditMessageFactory;
import com.Innova.bank.common.exception.ForbiddenException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.Innova.bank.common.constant.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final SessionService sessionService;
    private final TokenService tokenService;
    private final SessionValidator sessionValidator;
    private final AuthMapper authMapper;
    private final AuditFacade auditFacade;
    private final AuditMessageFactory auditMessageFactory;

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {

        SessionToken sessionToken;

        try {

            sessionToken = sessionService.findActiveByRefreshToken(request.getRefreshToken());

        } catch (Exception ex) {
            throw new UnauthorizedException(REFRESH_TOKEN_INVALID);
        }

        try {

            sessionValidator.validateUsableForRefresh(sessionToken);

        } catch (UnauthorizedException ex) {

            sessionService.deactivateSession(sessionToken);

            auditFacade.failed(sessionToken.getUser().getId(), sessionToken.getUser().getEmail(),
                    AuditAction.REFRESH_TOKEN, ex.getMessage(), httpRequest);

            throw ex;
        }

        User user = sessionToken.getUser();

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException(USER_BLOCKED);
        }

        UserDetails userDetails = buildUserDetails(user);

        if (!tokenService.isTokenValid(request.getRefreshToken(), userDetails)) {

            sessionService.deactivateAllUserSessions(user);

            auditFacade.failed(user.getId(), user.getEmail(), AuditAction.REFRESH_TOKEN,
                    auditMessageFactory.refreshSuspicious(), httpRequest);

            throw new UnauthorizedException(REFRESH_TOKEN_INVALID);
        }

        String newAccessToken = tokenService.generateAccessToken(userDetails, sessionToken.getSessionId());

        sessionService.updateAccessToken(sessionToken, newAccessToken);

        auditFacade.success(user.getId(), user.getEmail(), AuditAction.REFRESH_TOKEN, REFRESH_TOKEN_SUCCESS, httpRequest);

        return authMapper.toAuthResponse(newAccessToken, sessionToken.getRefreshToken(), tokenService.getAccessExpiration(), tokenService.getRefreshExpiration());
    }
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}
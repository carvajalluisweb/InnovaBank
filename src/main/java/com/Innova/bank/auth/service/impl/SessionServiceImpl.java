package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.entity.User;
import com.Innova.bank.auth.repository.SessionTokenRepository;
import com.Innova.bank.auth.service.SessionService;
import com.Innova.bank.auth.service.TokenService;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionTokenRepository sessionTokenRepository;
    private final TokenService tokenService;

    @Override
    public void deactivateAllUserSessions(User user) {
        List<SessionToken> activeSessions = sessionTokenRepository.findByUserAndActiveTrue(user);

        if (activeSessions.isEmpty()) {
            return;
        }

        for (SessionToken session : activeSessions) {
            session.setActive(false);
        }

        sessionTokenRepository.saveAll(activeSessions);
    }

    @Override
    public SessionToken createSession(User user, String sessionId, String accessToken, String refreshToken) {
        LocalDateTime now = LocalDateTime.now();

        long accessExpirationSeconds = tokenService.getAccessExpiration() / 1000;
        long refreshExpirationSeconds = tokenService.getRefreshExpiration() / 1000;

        SessionToken sessionToken = SessionToken.builder()
                .sessionId(sessionId)
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .active(true)
                .createdAt(now)
                .accessExpiresAt(now.plusSeconds(accessExpirationSeconds))
                .refreshExpiresAt(now.plusSeconds(refreshExpirationSeconds))
                .build();

        return sessionTokenRepository.save(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionToken findActiveByRefreshToken(String refreshToken) {
        return sessionTokenRepository.findByRefreshTokenAndActiveTrue(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no válido"));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionToken findActiveBySessionId(String sessionId) {
        return sessionTokenRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));
    }

    @Override
    public void deactivateSession(SessionToken sessionToken) {
        sessionToken.setActive(false);
        sessionTokenRepository.save(sessionToken);
    }

    @Override
    public void updateAccessToken(SessionToken sessionToken, String newAccessToken) {
        LocalDateTime now = LocalDateTime.now();
        long accessExpirationSeconds = tokenService.getAccessExpiration() / 1000;

        sessionToken.setAccessToken(newAccessToken);
        sessionToken.setAccessExpiresAt(now.plusSeconds(accessExpirationSeconds));

        sessionTokenRepository.save(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionId) {
        return sessionTokenRepository.findBySessionIdAndActiveTrue(sessionId).isPresent();
    }
}
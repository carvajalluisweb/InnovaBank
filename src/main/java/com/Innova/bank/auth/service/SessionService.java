package com.Innova.bank.auth.service;

import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.entity.User;

public interface SessionService {

    void deactivateAllUserSessions(User user);

    SessionToken createSession(User user, String sessionId, String accessToken, String refreshToken);

    SessionToken findActiveByRefreshToken(String refreshToken);

    SessionToken findActiveBySessionId(String sessionId);

    void deactivateSession(SessionToken sessionToken);

    void updateAccessToken(SessionToken sessionToken, String newAccessToken);

    boolean isSessionActive(String sessionId);
}
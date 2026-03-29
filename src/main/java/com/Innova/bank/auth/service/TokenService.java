package com.Innova.bank.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenService {

    String generateAccessToken(UserDetails userDetails, String sessionId);

    String generateRefreshToken(UserDetails userDetails, String sessionId);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractUsername(String token);

    String extractSessionId(String token);

    String extractTokenType(String token);

    long getAccessExpiration();

    long getRefreshExpiration();
}

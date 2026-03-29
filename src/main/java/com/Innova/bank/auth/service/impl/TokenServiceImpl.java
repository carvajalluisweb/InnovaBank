package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.security.JwtService;
import com.Innova.bank.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;

    @Override
    public String generateAccessToken(UserDetails userDetails, String sessionId) {
        return jwtService.generateAccessToken(userDetails, sessionId);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails, String sessionId) {
        return jwtService.generateRefreshToken(userDetails, sessionId);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return jwtService.isTokenValid(token, userDetails);
    }

    @Override
    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    @Override
    public String extractSessionId(String token) {
        return jwtService.extractSessionId(token);
    }

    @Override
    public String extractTokenType(String token) {
        return jwtService.extractTokenType(token);
    }

    @Override
    public long getAccessExpiration() {
        return jwtService.getAccessExpiration();
    }

    @Override
    public long getRefreshExpiration() {
        return jwtService.getRefreshExpiration();
    }
}
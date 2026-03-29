package com.Innova.bank.auth.mapper;

import com.Innova.bank.auth.dto.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(
            String accessToken,
            String refreshToken,
            long accessExpiration,
            long refreshExpiration
    ) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(accessExpiration)
                .refreshTokenExpiresIn(refreshExpiration)
                .build();
    }
}
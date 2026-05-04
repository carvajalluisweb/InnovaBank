package com.Innova.bank.auth.mapper;

import com.Innova.bank.auth.dto.AuthResponse;
import com.Innova.bank.common.constant.SecurityConstants;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, long accessExpiration, long refreshExpiration) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.TOKEN_TYPE_BEARER)
                .accessTokenExpiresIn(accessExpiration)
                .refreshTokenExpiresIn(refreshExpiration)
                .build();
    }
}
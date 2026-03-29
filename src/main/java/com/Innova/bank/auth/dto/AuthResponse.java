package com.Innova.bank.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
}

package com.Innova.bank.auth.service;

import com.Innova.bank.auth.dto.AuthResponse;
import com.Innova.bank.auth.dto.RefreshTokenRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenService {

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);
}
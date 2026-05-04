package com.Innova.bank.auth.service;

import com.Innova.bank.auth.dto.AuthResponse;
import com.Innova.bank.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface SessionAccessService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout(String authHeader, HttpServletRequest httpRequest);
}
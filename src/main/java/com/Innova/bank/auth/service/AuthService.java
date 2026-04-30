package com.Innova.bank.auth.service;

import com.Innova.bank.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    void registerCustomer(RegisterCustomerRequest request, HttpServletRequest httpRequest);

    void registerStaff(RegisterStaffRequest request, HttpServletRequest httpRequest);

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);

    void logout(String authHeader, HttpServletRequest httpRequest);

    ActualSessionResponse actualSession();
}
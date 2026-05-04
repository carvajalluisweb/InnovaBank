    package com.Innova.bank.auth.service.impl;

    import com.Innova.bank.auth.service.*;
    import com.Innova.bank.enums.*;
    import com.Innova.bank.auth.dto.*;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class AuthServiceImpl implements AuthService {

        private final RegistrationService registrationService;
        private final SessionAccessService sessionAccessService;
        private final RefreshTokenService refreshTokenService;


        @Override
        public void registerCustomer(RegisterCustomerRequest request, HttpServletRequest httpRequest) {
            registrationService.registerCustomer(request, httpRequest);
        }

        @Override
        public void registerStaff(RegisterStaffRequest request, HttpServletRequest httpRequest) {
            registrationService.registerStaff(request, httpRequest);
        }

        @Override
        public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
            return sessionAccessService.login(request, httpRequest);
        }

        @Override
        public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
            return refreshTokenService.refreshToken(request, httpRequest);
        }

        @Override
        public void logout(String authHeader, HttpServletRequest httpRequest) {
            sessionAccessService.logout(authHeader, httpRequest);
        }


    }
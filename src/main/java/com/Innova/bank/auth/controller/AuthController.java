package com.Innova.bank.auth.controller;

import com.Innova.bank.auth.dto.*;
import com.Innova.bank.auth.service.AuthService;
import com.Innova.bank.common.constant.RequestConstants;
import com.Innova.bank.common.response.ApiResponse;
import com.Innova.bank.common.response.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResponseFactory responseFactory;

    @PostMapping("/register-customer")
    public ResponseEntity<ApiResponse<Void>> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request, HttpServletRequest httpRequest) {
        authService.registerCustomer(request, httpRequest);

        return responseFactory.ok("Cliente registrado correctamente. Ahora puede iniciar sesión.");
    }

    @PostMapping("/register-staff")
    public ResponseEntity<ApiResponse<Void>> registerStaff(@Valid @RequestBody RegisterStaffRequest request, HttpServletRequest httpRequest) {
        authService.registerStaff(request, httpRequest);

        return responseFactory.ok("Empleado registrado correctamente. Ahora puede iniciar sesión.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse authResponse = authService.login(request, httpRequest);

        return responseFactory.ok("Login exitoso",authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        AuthResponse authResponse = authService.refreshToken(request, httpRequest);

        return responseFactory.ok("Token renovado correctamente",authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(RequestConstants.AUTHORIZATION_HEADER) String authHeader, HttpServletRequest httpRequest) {
        authService.logout(authHeader, httpRequest);

        return responseFactory.ok("Sesión cerrada correctamente");
    }

}
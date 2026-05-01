package com.Innova.bank.auth.controller;

import com.Innova.bank.auth.dto.*;
import com.Innova.bank.auth.service.AuthService;
import com.Innova.bank.common.response.ApiResponse;
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

    @PostMapping("/register-customer")
    public ResponseEntity<ApiResponse<Void>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.registerCustomer(request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cliente registrado correctamente. Ahora puede iniciar sesión.")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/register-staff")
    public ResponseEntity<ApiResponse<Void>> registerStaff(
            @Valid @RequestBody RegisterStaffRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.registerStaff(request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Empleado registrado correctamente. Ahora puede iniciar sesión.")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse authResponse = authService.login(request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login exitoso")
                        .data(authResponse)
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse authResponse = authService.refreshToken(request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token renovado correctamente")
                        .data(authResponse)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest
    ) {
        authService.logout(authHeader, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Sesión cerrada correctamente")
                        .data(null)
                        .build()
        );
    }

}
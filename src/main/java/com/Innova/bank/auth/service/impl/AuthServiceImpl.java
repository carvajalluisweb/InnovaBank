package com.Innova.bank.auth.service.impl;

import com.Innova.bank.audit.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.Innova.bank.auth.dto.*;
import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.auth.entity.User;
import com.Innova.bank.auth.mapper.AuthMapper;
import com.Innova.bank.auth.mapper.UserMapper;
import com.Innova.bank.auth.mapper.UserProfileMapper;
import com.Innova.bank.auth.repository.UserRepository;
import com.Innova.bank.auth.service.AuthService;
import com.Innova.bank.auth.service.SessionService;
import com.Innova.bank.auth.service.TokenService;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public void register(RegisterRequest request, HttpServletRequest httpRequest) {
        validateRegisterRequest(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword);

        userRepository.save(user);

        auditLogService.save(
                user.getId(),
                user.getEmail(),
                AuditAction.REGISTER,
                "Registro de usuario exitoso",
                AuditStatus.SUCCESS,
                httpRequest
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception ex) {
            auditLogService.save(
                    null,
                    request.getEmail(),
                    AuditAction.FAILED_LOGIN,
                    "Intento de inicio de sesión fallido",
                    AuditStatus.FAILED,
                    httpRequest
            );
            throw new BadRequestException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        sessionService.deactivateAllUserSessions(user);

        UserDetails userDetails = buildUserDetails(user);
        String sessionId = UUID.randomUUID().toString();

        String accessToken = tokenService.generateAccessToken(userDetails, sessionId);
        String refreshToken = tokenService.generateRefreshToken(userDetails, sessionId);

        sessionService.createSession(user, sessionId, accessToken, refreshToken);

        auditLogService.save(
                user.getId(),
                user.getEmail(),
                AuditAction.LOGIN,
                "Inicio de sesión exitoso",
                AuditStatus.SUCCESS,
                httpRequest
        );

        return authMapper.toAuthResponse(
                accessToken,
                refreshToken,
                tokenService.getAccessExpiration(),
                tokenService.getRefreshExpiration()
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        SessionToken sessionToken = sessionService.findActiveByRefreshToken(request.getRefreshToken());

        if (sessionToken.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            sessionService.deactivateSession(sessionToken);

            auditLogService.save(
                    sessionToken.getUser().getId(),
                    sessionToken.getUser().getEmail(),
                    AuditAction.REFRESH_TOKEN,
                    "Intento de renovación con refresh token expirado",
                    AuditStatus.FAILED,
                    httpRequest
            );

            throw new BadRequestException("El refresh token ha expirado");
        }

        User user = sessionToken.getUser();
        UserDetails userDetails = buildUserDetails(user);

        if (!tokenService.isTokenValid(request.getRefreshToken(), userDetails)) {
            auditLogService.save(
                    user.getId(),
                    user.getEmail(),
                    AuditAction.REFRESH_TOKEN,
                    "Intento de renovación con refresh token inválido",
                    AuditStatus.FAILED,
                    httpRequest
            );

            throw new BadRequestException("Refresh token inválido");
        }

        String newAccessToken = tokenService.generateAccessToken(userDetails, sessionToken.getSessionId());
        sessionService.updateAccessToken(sessionToken, newAccessToken);

        auditLogService.save(
                user.getId(),
                user.getEmail(),
                AuditAction.REFRESH_TOKEN,
                "Renovación de token exitosa",
                AuditStatus.SUCCESS,
                httpRequest
        );

        return authMapper.toAuthResponse(
                newAccessToken,
                sessionToken.getRefreshToken(),
                tokenService.getAccessExpiration(),
                tokenService.getRefreshExpiration()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActualSessionResponse actualSession() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Usuario no autenticado");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return userProfileMapper.toMeResponse(user);
    }

    @Override
    @Transactional
    public void logout(String authHeader, HttpServletRequest httpRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Token no proporcionado");
        }

        String token = authHeader.substring(7);
        String sessionId = tokenService.extractSessionId(token);

        SessionToken sessionToken = sessionService.findActiveBySessionId(sessionId);
        sessionService.deactivateSession(sessionToken);

        auditLogService.save(
                sessionToken.getUser().getId(),
                sessionToken.getUser().getEmail(),
                AuditAction.LOGOUT,
                "Cierre de sesión exitoso",
                AuditStatus.SUCCESS,
                httpRequest
        );
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El correo ya está registrado");
        }

        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BadRequestException("El número de cédula ya está registrado");
        }
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }


}
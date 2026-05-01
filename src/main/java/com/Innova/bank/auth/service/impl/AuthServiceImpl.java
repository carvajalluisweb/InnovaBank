    package com.Innova.bank.auth.service.impl;

    import com.Innova.bank.audit.service.AuditLogService;
    import com.Innova.bank.auth.repository.SessionTokenRepository;
    import com.Innova.bank.auth.service.AuthAttemptService;
    import com.Innova.bank.common.exception.ForbiddenException;
    import com.Innova.bank.common.exception.UnauthorizedException;
    import com.Innova.bank.enums.*;
    import com.Innova.bank.user.entity.UserCustomer;
    import com.Innova.bank.user.entity.UserStaff;
    import com.Innova.bank.user.repository.UserCustomerRepository;
    import com.Innova.bank.user.repository.UserStaffRepository;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import com.Innova.bank.auth.dto.*;
    import com.Innova.bank.auth.entity.SessionToken;
    import com.Innova.bank.user.entity.User;
    import com.Innova.bank.auth.mapper.AuthMapper;
    import com.Innova.bank.user.mapper.UserMapper;
    import com.Innova.bank.user.mapper.UserProfileMapper;
    import com.Innova.bank.user.repository.UserRepository;
    import com.Innova.bank.auth.service.AuthService;
    import com.Innova.bank.auth.service.SessionService;
    import com.Innova.bank.auth.service.TokenService;
    import com.Innova.bank.common.exception.BadRequestException;
    import com.Innova.bank.common.exception.ResourceNotFoundException;
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
        private final UserCustomerRepository userCustomerRepository;
        private final UserStaffRepository userStaffRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final SessionService sessionService;
        private final TokenService tokenService;
        private final AuditLogService auditLogService;
        private final AuthMapper authMapper;
        private final AuthAttemptService authAttemptService;
        private final SessionTokenRepository sessionTokenRepository;


        @Override
        @Transactional
        public void registerCustomer(RegisterCustomerRequest request, HttpServletRequest httpRequest) {
            User authenticatedUser = getAuthenticatedUser();

            try {

                validateRolUser(authenticatedUser);

                validateRegisterData(
                        request.getPassword(),
                        request.getConfirmPassword(),
                        request.getEmail()
                );

                validateCustomerIdNumber(request.getIdNumber());

                String encodedPassword = passwordEncoder.encode(request.getPassword());

                User user = User.builder()
                        .email(request.getEmail())
                        .password(encodedPassword)
                        .role(Rol.ROLE_USER)
                        .status(UserStatus.ACTIVE)
                        .build();

                User savedUser = userRepository.save(user);

                UserCustomer customer = UserCustomer.builder()
                        .idNumber(request.getIdNumber())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .phoneNumber(request.getPhoneNumber())
                        .age(request.getAge())
                        .gender(request.getGender())
                        .user(savedUser)
                        .build();

                userCustomerRepository.save(customer);

                auditLogService.save(
                        authenticatedUser.getId(),
                        authenticatedUser.getEmail(),
                        AuditAction.REGISTER_CUSTOMER,
                        "Cliente registrado: "
                                + request.getFirstName()
                                + " "
                                + request.getLastName()
                                + " - "
                                + request.getEmail(),
                        AuditStatus.SUCCESS,
                        httpRequest
                );

            } catch (Exception ex) {

                auditLogService.save(
                        authenticatedUser.getId(),
                        authenticatedUser.getEmail(),
                        AuditAction.REGISTER_CUSTOMER,
                        "Error al registrar cliente: "
                                + ex.getMessage(),
                        AuditStatus.FAILED,
                        httpRequest
                );

                throw ex;
            }
        }

        @Override
        @Transactional
        public void registerStaff(RegisterStaffRequest request, HttpServletRequest httpRequest) {

            User authenticatedUser = getAuthenticatedUser();

            try {

                validateAdminRole(authenticatedUser);

                validateRegisterData(request.getPassword(),  request.getConfirmPassword(), request.getEmail());

                validateInternalRole(request.getRole());

                validateStaffIdNumber(request.getIdNumber());

                validateRolePosition(request.getRole(), request.getPosition());

                String encodedPassword = passwordEncoder.encode(request.getPassword());

                User user = User.builder()
                        .email(request.getEmail())
                        .password(encodedPassword)
                        .role(request.getRole())
                        .status(UserStatus.ACTIVE)
                        .build();

                User savedUser = userRepository.save(user);

                UserStaff staff = UserStaff.builder()
                        .idNumber(request.getIdNumber())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .phoneNumber(request.getPhoneNumber())
                        .age(request.getAge())
                        .gender(request.getGender())
                        .position(String.valueOf(request.getPosition()))
                        .department(String.valueOf(Department.OPERATIONS))
                        .user(savedUser)
                        .build();

                UserStaff savedStaff = userStaffRepository.save(staff);

                savedStaff.setEmployeeCode(generateEmployeeCode(request.getPosition(), savedStaff.getId()));

                userStaffRepository.save(savedStaff);

                auditLogService.save(
                        authenticatedUser.getId(),
                        authenticatedUser.getEmail(),
                        AuditAction.REGISTER_STAFF,
                        "Usuario interno registrado: "
                                + request.getEmail()
                                + " Rol: "
                                + request.getRole().name()
                                + " Position: "
                                + request.getPosition().name(),
                        AuditStatus.SUCCESS,
                        httpRequest
                );

            } catch (Exception ex) {

                auditLogService.save(
                        authenticatedUser.getId(),
                        authenticatedUser.getEmail(),
                        AuditAction.REGISTER_STAFF,
                        "Error al registrar usuario interno: "
                                + ex.getMessage(),
                        AuditStatus.FAILED,
                        httpRequest
                );

                throw ex;
            }
        }

        @Override
        @Transactional
        public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new ForbiddenException("Usuario bloqueado. Contacte al administrador");
            }

            try {

                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            } catch (Exception ex) {

                authAttemptService.increaseFailedAttempts(user);

                User updatedUser = userRepository.findById(user.getId()).orElseThrow();

                auditLogService.save(
                        user.getId(),
                        user.getEmail(),
                        AuditAction.FAILED_LOGIN,
                        "Intento de inicio de sesión fallido",
                        AuditStatus.FAILED,
                        httpRequest
                );

                if (updatedUser.getStatus() == UserStatus.BLOCKED) {
                    throw new ForbiddenException("Usuario bloqueado por múltiples intentos fallidos. Contacte al administrador");
                }

                int remainingAttempts = 3 - updatedUser.getFailedAttempts();

                throw new BadRequestException("Credenciales inválidas. Intentos restantes: " + remainingAttempts);
            }

            authAttemptService.resetFailedAttempts(user);

            sessionService.deactivateAllUserSessions(user);

            UserDetails userDetails =  buildUserDetails(user);

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

            SessionToken sessionToken;

            try {

                sessionToken = sessionService.findActiveByRefreshToken(request.getRefreshToken());

            } catch (Exception ex) {

                handleRefreshSecurityIncident(request.getRefreshToken(), httpRequest);

                throw new UnauthorizedException("Refresh token inválido");
            }

            if (sessionToken.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {

                sessionService.deactivateSession(sessionToken);

                auditLogService.save(
                        sessionToken.getUser().getId(),
                        sessionToken.getUser().getEmail(),
                        AuditAction.REFRESH_TOKEN,
                        "Refresh token expirado",
                        AuditStatus.FAILED,
                        httpRequest
                );

                throw new UnauthorizedException(
                        "Refresh token expirado"
                );
            }

            User user = sessionToken.getUser();

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new ForbiddenException("Usuario bloqueado");
            }

            UserDetails userDetails = buildUserDetails(user);

            if (!tokenService.isTokenValid(request.getRefreshToken(), userDetails)) {

                sessionService.deactivateAllUserSessions(user);

                auditLogService.save(
                        user.getId(),
                        user.getEmail(),
                        AuditAction.REFRESH_TOKEN,
                        "Intento de reutilización o manipulación de refresh token",
                        AuditStatus.FAILED,
                        httpRequest
                );

                throw new UnauthorizedException(
                        "Refresh token inválido"
                );
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

            return authMapper.toAuthResponse(newAccessToken, sessionToken.getRefreshToken(), tokenService.getAccessExpiration(), tokenService.getRefreshExpiration());
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






        private void validateRegisterData(String password, String confirmPassword, String email) {
            if(!password.equals(confirmPassword)){
                throw new BadRequestException("Las contraseñas no son iguales");
            }

            if (userRepository.existsByEmail(email)){
                throw new BadRequestException("El correo ya está registrado");
            }

        }

        private void validateCustomerIdNumber(String idNumber) {

            if (userCustomerRepository.existsByIdNumber(idNumber)) {

                throw new BadRequestException("La cédula ya existe en clientes");
            }
        }

        private void validateStaffIdNumber(String idNumber) {

            if (userStaffRepository.existsByIdNumber(idNumber)) {
                throw new BadRequestException("La cédula ya existe en personal");
            }
        }

        private void validateRolePosition(Rol role, Position position) {

            if (role == Rol.ROLE_ADMIN && position != Position.GENERAL_MANAGER) {

                throw new BadRequestException(
                        "ROLE_ADMIN solo puede ser GENERAL_MANAGER"
                );
            }

            if (role == Rol.ROLE_OPERATOR && position != Position.TELLER && position != Position.CUSTOMER_SERVICE) {

                throw new BadRequestException(
                        "ROLE_OPERATOR solo puede ser TELLER o CUSTOMER_SERVICE"
                );
            }
        }

        private String generateEmployeeCode(Position position, Long id) {

            String prefix = switch (position) {

                case GENERAL_MANAGER -> "GM";
                case TELLER -> "TEL";
                case CUSTOMER_SERVICE -> "CSR";
            };

            return prefix + "-" +
                    String.format("%04d", id);
        }

        private void validateInternalRole(Rol role){

            if(role != Rol.ROLE_OPERATOR && role != Rol.ROLE_ADMIN){
                throw new BadRequestException("Solo se permite rol de ADMIN o OPERATOR");
            }
        }

        private UserDetails buildUserDetails(User user) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(user.getRole().name())
                    .build();
        }

        private User getAuthenticatedUser() {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName() == null) {

                throw new UnauthorizedException(
                        "Usuario no autenticado"
                );
            }

            return userRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Usuario autenticado no encontrado"
                            )
                    );
        }

        private void validateRolUser(User user) {
            if (user.getRole() != Rol.ROLE_OPERATOR && user.getRole() != Rol.ROLE_ADMIN) {
                throw new UnauthorizedException("No tiene permiso para realizar esta accion");
            }
        }

        private void validateAdminRole(User user) {

            if (user.getRole() != Rol.ROLE_ADMIN) {
                throw new UnauthorizedException("Solo ADMIN puede realizar esta acción");
            }
        }

        private void handleRefreshSecurityIncident(String refreshToken, HttpServletRequest httpRequest) {

            sessionTokenRepository.findByRefreshToken(refreshToken)
                    .ifPresent(sessionToken -> {

                        User user = sessionToken.getUser();

                        sessionService.deactivateAllUserSessions(user);

                        auditLogService.save(
                                user.getId(),
                                user.getEmail(),
                                AuditAction.REFRESH_TOKEN,
                                "Refresh token sospechoso detectado. Todas las sesiones cerradas",
                                AuditStatus.FAILED,
                                httpRequest
                        );
                    });
        }

    }
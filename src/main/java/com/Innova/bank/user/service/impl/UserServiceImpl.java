package com.Innova.bank.user.service.impl;

import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ForbiddenException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.mapper.UserProfileMapper;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public ActualSessionResponse getMyProfile() {
        User authenticatedUser = getAuthenticatedUser();
        return userProfileMapper.toMeResponse(authenticatedUser);
    }

    @Override
    public ActualSessionResponse updateMyProfile(
            UpdateMyProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            applyProfileUpdates(authenticatedUser, request);
            User updatedUser = userRepository.save(authenticatedUser);

            saveAuditSuccess(
                    updatedUser.getId(),
                    updatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Actualización de perfil exitosa",
                    httpRequest
            );

            return userProfileMapper.toMeResponse(updatedUser);

        } catch (Exception ex) {
            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Error al actualizar perfil: " + ex.getMessage(),
                    httpRequest
            );
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        User authenticatedUser = getAuthenticatedUser();
        validateOperatorOrAdmin(authenticatedUser);

        return userRepository.findAll()
                .stream()
                .map(userProfileMapper::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User authenticatedUser = getAuthenticatedUser();
        validateOperatorOrAdmin(authenticatedUser);

        User targetUser = findUserById(id);
        return userProfileMapper.toUserResponse(targetUser);
    }

    @Override
    public UserResponse updateUserRole(
            Long id,
            UpdateUserRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            validateAdmin(authenticatedUser);

            User targetUser = findUserById(id);
            validateNotSelfOperation(authenticatedUser, targetUser, "No puedes cambiar tu propio rol");
            validateRoleChange(targetUser, request);

            targetUser.setRole(request.getRole());
            User updatedUser = userRepository.save(targetUser);

            saveAuditSuccess(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Cambio de rol al usuario " + updatedUser.getEmail() + " a " + request.getRole().name(),
                    httpRequest
            );

            return userProfileMapper.toUserResponse(updatedUser);

        } catch (Exception ex) {
            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Error al cambiar rol: " + ex.getMessage(),
                    httpRequest
            );
            throw ex;
        }
    }

    @Override
    public UserResponse updateUserStatus(
            Long id,
            UpdateUserStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        User authenticatedUser = getAuthenticatedUser();

        try {
            validateAdmin(authenticatedUser);

            User targetUser = findUserById(id);
            validateNotSelfOperation(authenticatedUser, targetUser, "No puedes cambiar tu propio estado");
            validateStatusChange(targetUser, request);

            targetUser.setStatus(request.getStatus());
            User updatedUser = userRepository.save(targetUser);

            saveAuditSuccess(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_USER_STATUS,
                    "Cambio de estado al usuario " + updatedUser.getEmail() + " a " + request.getStatus().name(),
                    httpRequest
            );

            return userProfileMapper.toUserResponse(updatedUser);

        } catch (Exception ex) {
            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_USER_STATUS,
                    "Error al cambiar estado de usuario: " + ex.getMessage(),
                    httpRequest
            );
            throw ex;
        }
    }

    private void applyProfileUpdates(User user, UpdateMyProfileRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    private void validateAdmin(User user) {
        if (user.getRole() != Rol.ROLE_ADMIN) {
            throw new ForbiddenException("No tienes permisos para realizar esta acción");
        }
    }

    private void validateOperatorOrAdmin(User user) {
        if (user.getRole() != Rol.ROLE_OPERATOR && user.getRole() != Rol.ROLE_ADMIN) {
            throw new ForbiddenException("No tienes permisos para realizar esta acción");
        }
    }

    private void validateNotSelfOperation(User authenticatedUser, User targetUser, String message) {
        if (authenticatedUser.getId().equals(targetUser.getId())) {
            throw new BadRequestException(message);
        }
    }

    private void validateRoleChange(User targetUser, UpdateUserRoleRequest request) {
        if (targetUser.getRole() == request.getRole()) {
            throw new BadRequestException("El usuario ya tiene asignado ese rol");
        }
    }

    private void validateStatusChange(User targetUser, UpdateUserStatusRequest request) {
        if (targetUser.getStatus() == request.getStatus()) {
            throw new BadRequestException("El usuario ya tiene asignado ese estado");
        }
    }

    private void saveAuditSuccess(
            Long userId,
            String email,
            AuditAction action,
            String description,
            HttpServletRequest httpRequest
    ) {
        auditLogService.save(
                userId,
                email,
                action,
                description,
                AuditStatus.SUCCESS,
                httpRequest
        );
    }

    private void saveAuditFailure(
            Long userId,
            String email,
            AuditAction action,
            String description,
            HttpServletRequest httpRequest
    ) {
        auditLogService.save(
                userId,
                email,
                action,
                description,
                AuditStatus.FAILED,
                httpRequest
        );
    }
}
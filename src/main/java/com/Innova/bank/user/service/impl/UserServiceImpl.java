package com.Innova.bank.user.service.impl;

import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.AuditStatus;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
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
            authenticatedUser.setFirstName(request.getFirstName());
            authenticatedUser.setLastName(request.getLastName());
            authenticatedUser.setPhoneNumber(request.getPhoneNumber());
            authenticatedUser.setAge(request.getAge());
            authenticatedUser.setGender(request.getGender());

            User updatedUser = userRepository.save(authenticatedUser);

            auditLogService.save(
                    updatedUser.getId(),
                    updatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Actualización de perfil exitosa",
                    AuditStatus.SUCCESS,
                    httpRequest
            );

            return userProfileMapper.toMeResponse(updatedUser);

        } catch (Exception ex) {
            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Error al actualizar perfil: " + ex.getMessage(),
                    AuditStatus.FAILED,
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

            if (authenticatedUser.getId().equals(targetUser.getId())) {
                throw new BadRequestException("No puedes cambiar tu propio rol");
            }

            if (targetUser.getRole() == request.getRole()) {
                throw new BadRequestException("El usuario ya tiene asignado ese rol");
            }

            targetUser.setRole(request.getRole());
            User updatedUser = userRepository.save(targetUser);

            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Cambio de rol al usuario " + updatedUser.getEmail() + " a " + request.getRole().name(),
                    AuditStatus.SUCCESS,
                    httpRequest
            );

            return userProfileMapper.toUserResponse(updatedUser);

        } catch (Exception ex) {
            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Error al cambiar rol: " + ex.getMessage(),
                    AuditStatus.FAILED,
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

            if (authenticatedUser.getId().equals(targetUser.getId())) {
                throw new BadRequestException("No puedes cambiar tu propio estado");
            }

            if (targetUser.getStatus() == request.getStatus()) {
                throw new BadRequestException("El usuario ya tiene asignado ese estado");
            }

            targetUser.setStatus(request.getStatus());
            User updatedUser = userRepository.save(targetUser);

            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_USER_STATUS,
                    "Cambio de estado al usuario " + updatedUser.getEmail() + " a " + request.getStatus().name(),
                    AuditStatus.SUCCESS,
                    httpRequest
            );

            return userProfileMapper.toUserResponse(updatedUser);

        } catch (Exception ex) {
            auditLogService.save(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_USER_STATUS,
                    "Error al cambiar estado de usuario: " + ex.getMessage(),
                    AuditStatus.FAILED,
                    httpRequest
            );
            throw ex;
        }
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
            throw new UnauthorizedException("No tienes permisos para realizar esta acción");
        }
    }

    private void validateOperatorOrAdmin(User user) {
        if (user.getRole() != Rol.ROLE_OPERATOR && user.getRole() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("No tienes permisos para realizar esta acción");
        }
    }
}
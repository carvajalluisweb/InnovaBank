package com.Innova.bank.user.service.impl;

import com.Innova.bank.audit.service.AuditLogService;
import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.exception.ForbiddenException;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.enums.*;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import com.Innova.bank.user.mapper.UserProfileMapper;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;
    private final UserProfileMapper userProfileMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public ActualSessionResponse getMyProfile() {

        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getRole() == Rol.ROLE_USER) {

            UserCustomer customer = userCustomerRepository.findByUser(authenticatedUser)
                            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

            return userProfileMapper.toCustomerSessionResponse(authenticatedUser, customer);
        }

        UserStaff staff = userStaffRepository.findByUser(authenticatedUser)
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

        return userProfileMapper.toStaffSessionResponse(authenticatedUser, staff);
    }

    @Override
    public ActualSessionResponse updateMyProfile(UpdateMyProfileRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = getAuthenticatedUser();

        try {

            if (authenticatedUser.getRole() == Rol.ROLE_USER) {

                UserCustomer customer = userCustomerRepository.findByUser(authenticatedUser)
                                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

                customer.setFirstName(request.getFirstName());
                customer.setLastName(request.getLastName());
                customer.setPhoneNumber(request.getPhoneNumber());
                customer.setAge(request.getAge());
                customer.setGender(request.getGender());

                userCustomerRepository.save(customer);

                saveAuditSuccess(
                        authenticatedUser.getId(),
                        authenticatedUser.getEmail(),
                        AuditAction.UPDATE_PROFILE,
                        "Cliente actualizado",
                        httpRequest
                );

                return userProfileMapper.toCustomerSessionResponse(authenticatedUser, customer);
            }

            UserStaff staff = userStaffRepository.findByUser(authenticatedUser)
                            .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

            staff.setFirstName(request.getFirstName());
            staff.setLastName(request.getLastName());
            staff.setPhoneNumber(request.getPhoneNumber());
            staff.setAge(request.getAge());
            staff.setGender(request.getGender());

            userStaffRepository.save(staff);

            saveAuditSuccess(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Empleado actualizado",
                    httpRequest
            );

            return userProfileMapper.toStaffSessionResponse(authenticatedUser, staff);

        } catch (Exception ex) {

            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    "Error actualizando perfil: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllStaff() {

        User authenticatedUser = getAuthenticatedUser();

        validateOperatorOrAdmin(authenticatedUser);

        List<User> users = userRepository.findByRoleIn(
                List.of(
                        Rol.ROLE_ADMIN,
                        Rol.ROLE_OPERATOR
                )
        );

        List<UserResponse> response = new ArrayList<>();

        for (User user : users) {

            userStaffRepository.findByUser(user)
                    .ifPresent(staff ->
                            response.add(
                                    userProfileMapper.toStaffUserResponse(
                                            user,
                                            staff
                                    )
                            )
                    );
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllCustomers() {

        User authenticatedUser = getAuthenticatedUser();

        validateOperatorOrAdmin(authenticatedUser);

        List<User> users = userRepository.findByRole(Rol.ROLE_USER);

        List<UserResponse> response = new ArrayList<>();

        for (User user : users) {

            userCustomerRepository.findByUser(user)
                    .ifPresent(customer ->
                            response.add(
                                    userProfileMapper.toCustomerUserResponse(
                                            user,
                                            customer
                                    )
                            )
                    );
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User authenticatedUser = getAuthenticatedUser();

        validateOperatorOrAdmin(authenticatedUser);

        User targetUser = findUserById(id);

        if (targetUser.getRole() == Rol.ROLE_USER) {

            UserCustomer customer = userCustomerRepository.findByUser(targetUser)
                            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

            return userProfileMapper.toCustomerUserResponse(targetUser, customer);
        }

        UserStaff staff = userStaffRepository.findByUser(targetUser)
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

        return userProfileMapper.toStaffUserResponse(targetUser, staff);
    }

    @Override
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = getAuthenticatedUser();

        try {

            validateAdmin(authenticatedUser);

            User targetUser = findUserById(id);

            validateNotSelfOperation(authenticatedUser, targetUser, "No puedes cambiar tu propio rol");

            validateRoleChange(targetUser, request);

            UserStaff staff = userStaffRepository.findByUser(targetUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

            targetUser.setRole(request.getRole());

            Position newPosition = resolvePositionByRole(request.getRole());

            staff.setPosition(newPosition.name());
            staff.setEmployeeCode(generateEmployeeCode(newPosition, staff.getId()));

            User updatedUser = userRepository.save(targetUser);
            UserStaff updatedStaff = userStaffRepository.save(staff);

            saveAuditSuccess(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Rol actualizado a "
                            + request.getRole().name()
                            + " | Nueva posición: "
                            + newPosition.name()
                            + " | Usuario: "
                            + updatedUser.getEmail(),
                    httpRequest
            );

            return userProfileMapper.toStaffUserResponse(updatedUser, updatedStaff);

        } catch (Exception ex) {

            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_ROLE,
                    "Error actualizando rol: " + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    @Override
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = getAuthenticatedUser();

        try {

            validateAdmin(authenticatedUser);

            User targetUser = findUserById(id);

            validateNotSelfOperation(authenticatedUser, targetUser, "No puedes cambiar tu propio estado");

            validateStatusChange(targetUser, request);

            targetUser.setStatus(request.getStatus());

            AuditAction action;
            String description;

            if (request.getStatus() == UserStatus.ACTIVE) {

                targetUser.setFailedAttempts(0);
                targetUser.setBlockedAt(null);

                action = AuditAction.USER_UNBLOCKED;

                description = "Usuario desbloqueado: " + targetUser.getEmail();

            } else {

                action = AuditAction.UPDATE_USER_STATUS;

                description = "Estado actualizado a " + request.getStatus().name()  + " usuario: " + targetUser.getEmail();
            }

            User updatedUser = userRepository.save(targetUser);

            saveAuditSuccess(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    action,
                    description,
                    httpRequest
            );

            return getUserById(
                    updatedUser.getId()
            );

        } catch (Exception ex) {

            saveAuditFailure(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_USER_STATUS,
                    "Error actualizando estado: "
                            + ex.getMessage(),
                    httpRequest
            );

            throw ex;
        }
    }

    private User getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {

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

    private void validateNotSelfOperation(User authenticatedUser, User targetUser, String message ) {

        if (authenticatedUser.getId().equals(targetUser.getId())) {

            throw new BadRequestException(message);
        }
    }

    private void validateRoleChange(User targetUser, UpdateUserRoleRequest request) {

        Rol currentRole = targetUser.getRole();
        Rol newRole = request.getRole();

        if (currentRole == newRole) {
            throw new BadRequestException(
                    "El usuario ya tiene ese rol"
            );
        }

        boolean currentIsStaff =
                currentRole == Rol.ROLE_ADMIN ||
                        currentRole == Rol.ROLE_OPERATOR;

        boolean newIsStaff =
                newRole == Rol.ROLE_ADMIN ||
                        newRole == Rol.ROLE_OPERATOR;

        if (!currentIsStaff || !newIsStaff) {
            throw new BadRequestException(
                    "Solo se permite cambio entre ADMIN y OPERATOR"
            );
        }
    }

    private void validateStatusChange(User targetUser, UpdateUserStatusRequest request) {

        if (targetUser.getStatus() == request.getStatus()) {

            throw new BadRequestException("El usuario ya tiene ese estado");
        }
    }

    private void saveAuditSuccess(Long userId, String email, AuditAction action, String description, HttpServletRequest httpRequest) {

        auditLogService.save(
                userId,
                email,
                action,
                description,
                AuditStatus.SUCCESS,
                httpRequest
        );
    }

    private void saveAuditFailure(Long userId, String email, AuditAction action, String description, HttpServletRequest httpRequest) {

        auditLogService.save(
                userId,
                email,
                action,
                description,
                AuditStatus.FAILED,
                httpRequest
        );
    }

    private Position resolvePositionByRole(Rol role) {

        if (role == Rol.ROLE_ADMIN) {
            return Position.GENERAL_MANAGER;
        }

        if (role == Rol.ROLE_OPERATOR) {
            return Position.TELLER;
        }

        throw new BadRequestException(
                "Solo se permite cambio entre ADMIN y OPERATOR"
        );
    }

    private String generateEmployeeCode(Position position, Long id) {

        String prefix = switch (position) {
            case GENERAL_MANAGER -> "GM";
            case TELLER -> "TEL";
            case CUSTOMER_SERVICE -> "CSR";
        };

        return prefix + "-" + String.format("%04d", id);
    }
}
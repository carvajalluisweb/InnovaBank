package com.Innova.bank.user.service.impl;

import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.audit.AuditMessageFactory;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.security.AuthorizationService;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.enums.*;
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
import com.Innova.bank.user.service.EmployeeCodeService;
import com.Innova.bank.user.service.UserFinderService;
import com.Innova.bank.user.service.UserManagementService;
import com.Innova.bank.user.service.UserResponseBuilderService;
import com.Innova.bank.user.validation.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.Innova.bank.common.constant.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final AuditFacade auditFacade;

    private final UserFinderService userFinderService;
    private final ValidationService validationService;
    private final EmployeeCodeService employeeCodeService;

    private final UserRepository userRepository;
    private final UserStaffRepository userStaffRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserResponseBuilderService userResponseBuilderService;
    private final AuditMessageFactory auditMessageFactory;

    @Override
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireAdmin(authenticatedUser);

            User targetUser = userFinderService.findById(id);

            authorizationService.requireNotSelf(
                    authenticatedUser,
                    targetUser.getId(),
                    "No puedes cambiar tu propio rol"
            );

            validationService.validateRoleChange(targetUser, request);

            UserStaff staff = userStaffRepository.findByUser(targetUser)
                    .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_STAFF));

            targetUser.setRole(request.getRole());

            Position newPosition = resolvePositionByRole(request.getRole());

            staff.setPosition(newPosition.name());

            staff.setEmployeeCode(employeeCodeService.generate(newPosition, staff.getId()));

            User updatedUser = userRepository.save(targetUser);

            UserStaff updatedStaff = userStaffRepository.save(staff);

            auditFacade.success(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.UPDATE_ROLE,
                    auditMessageFactory.roleUpdated(updatedUser.getEmail(), request.getRole()), httpRequest);

            return userProfileMapper.toStaffUserResponse(updatedUser, updatedStaff);

        } catch (Exception ex) {

            auditFacade.failed(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.UPDATE_ROLE, auditMessageFactory.error(
                            "Error actualizando rol: ", ex), httpRequest);

            throw ex;
        }
    }

    @Override
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireAdmin(authenticatedUser);

            User targetUser = userFinderService.findById(id);

            authorizationService.requireNotSelf(authenticatedUser, targetUser.getId(),"No puedes cambiar tu propio estado");

            validationService.validateStatusChange(targetUser, request);

            targetUser.setStatus(request.getStatus());

            if (request.getStatus() == UserStatus.ACTIVE) {
                targetUser.setFailedAttempts(0);
                targetUser.setBlockedAt(null);
            }

            User updatedUser = userRepository.save(targetUser);

            auditFacade.success(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.UPDATE_USER_STATUS,
                    auditMessageFactory.statusUpdated(updatedUser.getEmail(), request.getStatus()), httpRequest);

            return userResponseBuilderService.build(updatedUser);

        } catch (Exception ex) {

            auditFacade.failed(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.UPDATE_USER_STATUS,
                    auditMessageFactory.error("Error actualizando estado: ", ex), httpRequest);

            throw ex;
        }
    }



    private Position resolvePositionByRole(Rol role) {

        if (role == Rol.ROLE_ADMIN) {
            return Position.GENERAL_MANAGER;
        }

        return Position.TELLER;
    }
}
package com.Innova.bank.user.service.impl;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.enums.AuditAction;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import com.Innova.bank.user.mapper.UserProfileMapper;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.Innova.bank.common.constant.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final CurrentUserService currentUserService;
    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;
    private final UserProfileMapper userProfileMapper;
    private final AuditFacade auditFacade;

    @Override
    @Transactional(readOnly = true)
    public ActualSessionResponse getMyProfile() {

        User authenticatedUser = currentUserService.getCurrentUser();

        if (authenticatedUser.getRole() == Rol.ROLE_USER) {

            UserCustomer customer = userCustomerRepository .findByUser(authenticatedUser)
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_CLIENT));

            return userProfileMapper .toCustomerSessionResponse(authenticatedUser, customer);
        }

        UserStaff staff = userStaffRepository.findByUser(authenticatedUser)
                        .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_STAFF));

        return userProfileMapper.toStaffSessionResponse(authenticatedUser, staff);
    }

    @Override
    public ActualSessionResponse updateMyProfile(UpdateMyProfileRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        if (authenticatedUser.getRole() == Rol.ROLE_USER) {

            UserCustomer customer = userCustomerRepository.findByUser(authenticatedUser)
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_CLIENT));

            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setAge(request.getAge());
            customer.setGender(request.getGender());

            userCustomerRepository.save(customer);

            auditFacade.success(
                    authenticatedUser.getId(),
                    authenticatedUser.getEmail(),
                    AuditAction.UPDATE_PROFILE,
                    UPDATE_CLIENT,
                    httpRequest
            );

            return userProfileMapper.toCustomerSessionResponse(authenticatedUser, customer);
        }

        UserStaff staff = userStaffRepository.findByUser(authenticatedUser)
                        .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_STAFF));

        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setAge(request.getAge());
        staff.setGender(request.getGender());

        userStaffRepository.save(staff);

        auditFacade.success(
                authenticatedUser.getId(),
                authenticatedUser.getEmail(),
                AuditAction.UPDATE_PROFILE,
                UPDATE_STAFF,
                httpRequest
        );

        return userProfileMapper.toStaffSessionResponse(authenticatedUser,staff);
    }
}
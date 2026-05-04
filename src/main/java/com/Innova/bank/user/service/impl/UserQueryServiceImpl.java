package com.Innova.bank.user.service.impl;

import com.Innova.bank.common.security.AuthorizationService;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.mapper.UserProfileMapper;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.service.UserFinderService;
import com.Innova.bank.user.service.UserQueryService;
import com.Innova.bank.user.service.UserResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final UserResponseBuilderService userResponseBuilderService;

    private final UserRepository userRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;

    private final UserFinderService userFinderService;
    private final UserProfileMapper userProfileMapper;

    @Override
    public List<UserResponse> getAllStaff() {

        User authenticatedUser = currentUserService.getCurrentUser();

        authorizationService.requireOperatorOrAdmin(authenticatedUser);

        return userRepository.findByRoleIn(
                        List.of(Rol.ROLE_ADMIN, Rol.ROLE_OPERATOR)).stream()
                .map(user -> userStaffRepository.findByUser(user)
                        .map(staff -> userProfileMapper.toStaffUserResponse(user, staff))
                        .orElse(null)).filter(item -> item != null)
                .toList();
    }

    @Override
    public List<UserResponse> getAllCustomers() {

        User authenticatedUser =
                currentUserService.getCurrentUser();

        authorizationService.requireOperatorOrAdmin(
                authenticatedUser
        );

        return userRepository.findByRole(Rol.ROLE_USER).stream()
                .map(user -> userCustomerRepository.findByUser(user)
                        .map(customer -> userProfileMapper.toCustomerUserResponse(user, customer))
                        .orElse(null)).filter(item -> item != null)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {

        User authenticatedUser = currentUserService.getCurrentUser();

        authorizationService.requireOperatorOrAdmin(authenticatedUser);

        User targetUser = userFinderService.findById(id);

        return userResponseBuilderService.build(targetUser);
    }
}
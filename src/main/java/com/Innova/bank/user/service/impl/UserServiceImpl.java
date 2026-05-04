package com.Innova.bank.user.service.impl;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.user.service.*;
import com.Innova.bank.enums.*;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {


    private final UserProfileService userProfileService;
    private final UserQueryService userQueryService;
    private final UserManagementService userManagementService;

    @Override
    public ActualSessionResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @Override
    public ActualSessionResponse updateMyProfile(UpdateMyProfileRequest request, HttpServletRequest httpRequest) {
        return userProfileService.updateMyProfile(request, httpRequest);
    }

    @Override
    public List<UserResponse> getAllStaff() {
        return userQueryService.getAllStaff();
    }

    @Override
    public List<UserResponse> getAllCustomers() {
        return userQueryService.getAllCustomers();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userQueryService.getUserById(id);
    }

    @Override
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, HttpServletRequest httpRequest) {
        return userManagementService.updateUserRole(id, request, httpRequest);
    }

    @Override
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, HttpServletRequest httpRequest) {
        return userManagementService.updateUserStatus(id, request, httpRequest);
    }


}
package com.Innova.bank.user.service;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    ActualSessionResponse getMyProfile();

    ActualSessionResponse updateMyProfile(UpdateMyProfileRequest request, HttpServletRequest httpRequest);

    List<UserResponse> getAllStaff();

    List<UserResponse> getAllCustomers();

    UserResponse getUserById(Long id);

    UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, HttpServletRequest httpRequest);

    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, HttpServletRequest httpRequest);
}
package com.Innova.bank.user.service;

import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserManagementService {

    UserResponse updateUserRole(Long id, UpdateUserRoleRequest request, HttpServletRequest httpRequest);

    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request,HttpServletRequest httpRequest);
}
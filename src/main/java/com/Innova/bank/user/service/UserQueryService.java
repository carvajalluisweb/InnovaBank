package com.Innova.bank.user.service;

import com.Innova.bank.user.dto.UserResponse;

import java.util.List;

public interface UserQueryService {

    List<UserResponse> getAllStaff();

    List<UserResponse> getAllCustomers();

    UserResponse getUserById(Long id);
}
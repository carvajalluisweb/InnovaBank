package com.Innova.bank.user.service;

import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;

public interface UserResponseBuilderService {

    UserResponse build(User user);
}
package com.Innova.bank.user.mapper;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public ActualSessionResponse toMeResponse(User user) {
        return ActualSessionResponse.builder()
                .id(user.getId())
                .idNumber(user.getIdNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .gender(user.getGender().name())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .idNumber(user.getIdNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .gender(user.getGender().name())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}
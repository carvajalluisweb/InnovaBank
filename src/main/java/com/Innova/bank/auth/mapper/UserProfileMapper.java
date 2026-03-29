package com.Innova.bank.auth.mapper;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.auth.entity.User;
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
                .build();
    }
}
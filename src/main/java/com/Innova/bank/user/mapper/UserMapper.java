package com.Innova.bank.user.mapper;

import com.Innova.bank.auth.dto.RegisterRequest;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String encodedPassword) {
        return User.builder()
                .idNumber(request.getIdNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .age(request.getAge())
                .gender(request.getGender())
                .role(Rol.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
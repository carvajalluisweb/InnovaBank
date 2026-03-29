package com.Innova.bank.auth.mapper;

import com.Innova.bank.auth.dto.RegisterRequest;
import com.Innova.bank.auth.entity.User;
import com.Innova.bank.enums.Rol;
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
                .role(Rol.ROL_USER)
                .build();
    }
}
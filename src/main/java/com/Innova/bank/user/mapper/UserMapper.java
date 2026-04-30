package com.Innova.bank.user.mapper;

import com.Innova.bank.auth.dto.RegisterCustomerRequest;
import com.Innova.bank.auth.dto.RegisterStaffRequest;
import com.Innova.bank.enums.Department;
import com.Innova.bank.enums.Position;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toCustomerUserEntity(RegisterCustomerRequest request, String encodedPassword) {

        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Rol.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public UserCustomer toCustomerProfileEntity(RegisterCustomerRequest request, User user) {

        return UserCustomer.builder()
                .idNumber(request.getIdNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .age(request.getAge())
                .gender(request.getGender())
                .user(user)
                .build();
    }

    public User toStaffUserEntity(RegisterStaffRequest request, String encodedPassword) {

        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();
    }

    public UserStaff toStaffProfileEntity(RegisterStaffRequest request, User user, String employeeCode) {

        return UserStaff.builder()
                .idNumber(request.getIdNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .age(request.getAge())
                .gender(request.getGender())
                .position(request.getPosition().name())
                .department(Department.OPERATIONS.name())
                .employeeCode(employeeCode)
                .user(user)
                .build();
    }
}
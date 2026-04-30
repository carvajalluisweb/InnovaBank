package com.Innova.bank.user.mapper;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public ActualSessionResponse toCustomerSessionResponse(User user, UserCustomer customer) {

        return ActualSessionResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .idNumber(customer.getIdNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .age(customer.getAge())
                .gender(customer.getGender().name())
                .build();
    }

    public ActualSessionResponse toStaffSessionResponse(User user, UserStaff staff) {

        return ActualSessionResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .idNumber(staff.getIdNumber())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .phoneNumber(staff.getPhoneNumber())
                .age(staff.getAge())
                .gender(staff.getGender().name())
                .position(staff.getPosition())
                .department(staff.getDepartment())

                .build();
    }

    // ===================================================
    // USER RESPONSE CUSTOMER
    // ===================================================
    public UserResponse toCustomerUserResponse(
            User user,
            UserCustomer customer
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())

                .idNumber(customer.getIdNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .age(customer.getAge())
                .gender(customer.getGender().name())

                .build();
    }

    // ===================================================
    // USER RESPONSE STAFF
    // ===================================================
    public UserResponse toStaffUserResponse(
            User user,
            UserStaff staff
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())

                .idNumber(staff.getIdNumber())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .phoneNumber(staff.getPhoneNumber())
                .age(staff.getAge())
                .gender(staff.getGender().name())

                .build();
    }
}
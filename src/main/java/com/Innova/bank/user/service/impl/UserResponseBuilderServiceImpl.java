package com.Innova.bank.user.service.impl;

import com.Innova.bank.common.exception.ResourceNotFoundException;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import com.Innova.bank.user.mapper.UserProfileMapper;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.service.UserResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.Innova.bank.common.constant.MessageConstants.*;

@Service
@RequiredArgsConstructor
public class UserResponseBuilderServiceImpl implements UserResponseBuilderService {

    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserResponse build(User user) {

        if (user.getRole() == Rol.ROLE_USER) {

            UserCustomer customer = userCustomerRepository.findByUser(user)
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_CLIENT));

            return userProfileMapper.toCustomerUserResponse(user, customer);
        }

        UserStaff staff = userStaffRepository.findByUser(user)
                        .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_STAFF));

        return userProfileMapper.toStaffUserResponse(user, staff);
    }
}
package com.Innova.bank.user.service.impl;

import com.Innova.bank.common.constant.MessageConstants;
import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.service.UserFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.Innova.bank.common.constant.MessageConstants.NOT_FOUND_STAFF;

@Service
@RequiredArgsConstructor
public class UserFinderServiceImpl implements UserFinderService {

    private final UserRepository userRepository;
    private final ExceptionFactory exceptionFactory;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                        exceptionFactory.notFound(NOT_FOUND_STAFF + " con id: " + id));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        exceptionFactory.notFound(NOT_FOUND_STAFF + " con correo: " + email));
    }
}
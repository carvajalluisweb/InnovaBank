package com.Innova.bank.user.service;

import com.Innova.bank.user.entity.User;

public interface UserFinderService {

    User findById(Long id);

    User findByEmail(String email);
}
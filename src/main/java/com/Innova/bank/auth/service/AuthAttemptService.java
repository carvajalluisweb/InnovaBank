package com.Innova.bank.auth.service;

import com.Innova.bank.user.entity.User;

public interface AuthAttemptService {

    void increaseFailedAttempts(User user);

    void resetFailedAttempts(User user);
}
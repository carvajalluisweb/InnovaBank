package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.service.AuthAttemptService;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthAttemptServiceImpl implements AuthAttemptService {

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseFailedAttempts(User user) {

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow();

        int attempts =
                managedUser.getFailedAttempts() + 1;

        managedUser.setFailedAttempts(attempts);

        if (attempts >= 3) {
            managedUser.setStatus(UserStatus.BLOCKED);
            managedUser.setBlockedAt(LocalDateTime.now());
        }

        userRepository.save(managedUser);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(User user) {

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow();

        managedUser.setFailedAttempts(0);
        managedUser.setBlockedAt(null);

        userRepository.save(managedUser);
    }
}
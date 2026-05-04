package com.Innova.bank.common.security;

import com.Innova.bank.common.exception.UnauthorizedException;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.service.impl.UserFinderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.Innova.bank.common.constant.MessageConstants.USER_NOT_AUTHENTICATED;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserFinderServiceImpl userFinderService;

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {

            throw new UnauthorizedException(USER_NOT_AUTHENTICATED);
        }

        return userFinderService.findByEmail(authentication.getName());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }
}
package com.Innova.bank.auth.validation;

import com.Innova.bank.auth.entity.SessionToken;

public interface SessionValidator {

    void validateActive(SessionToken session);

    void validateRefreshNotExpired(SessionToken session);

    void validateUsableForRefresh(SessionToken session);
}
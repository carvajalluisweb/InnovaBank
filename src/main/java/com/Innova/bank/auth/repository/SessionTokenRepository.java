package com.Innova.bank.auth.repository;

import com.Innova.bank.auth.entity.SessionToken;
import com.Innova.bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {

    List<SessionToken> findByUserAndActiveTrue(User user);

    Optional<SessionToken> findByRefreshTokenAndActiveTrue(String refreshToken);

    Optional<SessionToken> findBySessionIdAndActiveTrue(String sessionId);
}

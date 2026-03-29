package com.Innova.bank.auth.repository;

import com.Innova.bank.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdNumber(String idNumber);
}

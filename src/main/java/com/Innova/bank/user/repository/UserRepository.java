package com.Innova.bank.user.repository;

import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleIn(List<Rol> roles);
    List<User> findByRole(Rol role);
 }

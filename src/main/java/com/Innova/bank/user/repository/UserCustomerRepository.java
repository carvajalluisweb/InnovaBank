package com.Innova.bank.user.repository;

import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCustomerRepository extends JpaRepository<UserCustomer, Long> {

    boolean existsByIdNumber(String idNumber);
    Optional<UserCustomer> findByIdNumber(String idNumber);
    Optional<UserCustomer> findByUser(User user);
}

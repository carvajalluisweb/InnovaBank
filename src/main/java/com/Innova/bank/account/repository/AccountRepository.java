package com.Innova.bank.account.repository;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    List<Account> findByUser(User user);

    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);
 }

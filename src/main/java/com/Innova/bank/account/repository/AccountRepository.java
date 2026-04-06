package com.Innova.bank.account.repository;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    List<Account> findByUser(User user);

    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT a
           FROM Account a
           WHERE a.accountNumber = :accountNumber
           """)
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
 }

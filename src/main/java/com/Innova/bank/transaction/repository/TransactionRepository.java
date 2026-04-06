package com.Innova.bank.transaction.repository;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(""" 
SELECT t
FROM Transaction t
WHERE t.originAccount.user = :user OR t.toAccount.user = :user
ORDER BY t.createdAt DESC
""")

    Page<Transaction> findMyTransactions(@Param("user") User user, Pageable pageable);

    Page<Transaction> findByOriginAccountOrToAccountOrderByCreatedAtDesc(Account originAccount, Account toAccount, Pageable pageable);

    @Query("""
           SELECT COALESCE(SUM(t.amount + t.fee), 0)
           FROM Transaction t
           WHERE t.originAccount = :originAccount
             AND t.status = :status
             AND t.transactionType IN :types
             AND t.createdAt BETWEEN :start AND :end
           """)

    BigDecimal sumDailyOutgoingWithFee(
            @Param("originAccount") Account originAccount,
            @Param("status") TransactionStatus status,
            @Param("types") List<TransactionType> types,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<Transaction> findByRequestId(String requestId);

}

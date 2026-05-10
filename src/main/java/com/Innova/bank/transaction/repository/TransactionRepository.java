package com.Innova.bank.transaction.repository;

import com.Innova.bank.account.entity.Account;
import com.Innova.bank.enums.TransactionStatus;
import com.Innova.bank.enums.TransactionType;
import com.Innova.bank.transaction.entity.Transaction;
import com.Innova.bank.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByRequestId(String requestId);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    @Query(
            value = "SELECT nextval('transaction_reference_seq')",
            nativeQuery = true
    )
    Long getNextTransactionSequence();

    @EntityGraph(attributePaths = {"originAccount", "originAccount.user", "toAccount", "toAccount.user"})
    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.originAccount.user = :user
           OR t.toAccount.user = :user
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findMyTransactions(User user,Pageable pageable);

    @EntityGraph(attributePaths = {"originAccount", "originAccount.user", "toAccount", "toAccount.user"})
    Page<Transaction> findByOriginAccountOrToAccountOrderByCreatedAtDesc(Account originAccount, Account toAccount, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(t.totalDebited), 0)
        FROM Transaction t
        WHERE t.originAccount = :originAccount
        AND t.status = :status
        AND t.transactionType IN :types
        AND t.createdAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumDailyOutgoingWithFee(Account originAccount, TransactionStatus status, List<TransactionType> types, LocalDateTime startDate, LocalDateTime endDate);
}
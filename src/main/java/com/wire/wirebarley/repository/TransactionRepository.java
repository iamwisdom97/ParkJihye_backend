package com.wire.wirebarley.repository;

import com.wire.wirebarley.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountNumberOrderByCreatedAtDesc(@Param("accountNumber") String accountNumber, Pageable pageable);
}
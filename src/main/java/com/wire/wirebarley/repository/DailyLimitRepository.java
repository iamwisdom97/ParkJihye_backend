package com.wire.wirebarley.repository;

import com.wire.wirebarley.domain.DailyLimit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyLimitRepository extends JpaRepository<DailyLimit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyLimit d WHERE d.accountNumber = :accountNumber AND d.transactionDate = :date")
    Optional<DailyLimit> findByAccountNumberAndTransactionDateWithLock(
            @Param("accountNumber") String accountNumber,
            @Param("date") LocalDate date);

    Optional<DailyLimit> findByAccountNumberAndTransactionDate(String accountNumber, LocalDate transactionDate);
}
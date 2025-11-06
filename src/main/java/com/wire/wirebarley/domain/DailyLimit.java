package com.wire.wirebarley.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_limits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"accountNumber", "transactionDate"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal withdrawalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal transferAmount;

    public DailyLimit(String accountNumber, LocalDate transactionDate) {
        this.accountNumber = accountNumber;
        this.transactionDate = transactionDate;
        this.withdrawalAmount = BigDecimal.ZERO;
        this.transferAmount = BigDecimal.ZERO;
    }

    public void addWithdrawalAmount(BigDecimal amount) {
        this.withdrawalAmount = this.withdrawalAmount.add(amount);
    }

    public void addTransferAmount(BigDecimal amount) {
        this.transferAmount = this.transferAmount.add(amount);
    }
}
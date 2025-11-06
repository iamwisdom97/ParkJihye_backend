package com.wire.wirebarley.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 20)
    private String targetAccountNumber; // For TRANSFER type

    @Column(precision = 19, scale = 2)
    private BigDecimal fee; // For TRANSFER type

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String description;

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount,
                      BigDecimal balanceAfter, String targetAccountNumber, BigDecimal fee, String description) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetAccountNumber = targetAccountNumber;
        this.fee = fee;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public static Transaction createDeposit(String accountNumber, BigDecimal amount, BigDecimal balanceAfter) {
        return new Transaction(accountNumber, TransactionType.DEPOSIT, amount, balanceAfter, null, null, "Deposit");
    }

    public static Transaction createWithdrawal(String accountNumber, BigDecimal amount, BigDecimal balanceAfter) {
        return new Transaction(accountNumber, TransactionType.WITHDRAWAL, amount, balanceAfter, null, null, "Withdrawal");
    }

    public static Transaction createTransferOut(String accountNumber, BigDecimal amount, BigDecimal balanceAfter,
                                               String targetAccountNumber, BigDecimal fee) {
        return new Transaction(accountNumber, TransactionType.TRANSFER_OUT, amount, balanceAfter,
                             targetAccountNumber, fee, "Transfer to " + targetAccountNumber);
    }

    public static Transaction createTransferIn(String accountNumber, BigDecimal amount, BigDecimal balanceAfter,
                                              String sourceAccountNumber) {
        return new Transaction(accountNumber, TransactionType.TRANSFER_IN, amount, balanceAfter,
                             sourceAccountNumber, null, "Transfer from " + sourceAccountNumber);
    }

    public static Transaction createFee(String accountNumber, BigDecimal fee, BigDecimal balanceAfter) {
        return new Transaction(accountNumber, TransactionType.FEE, fee, balanceAfter, null, null, "Transfer fee");
    }
}
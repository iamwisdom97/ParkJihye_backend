package com.wire.wirebarley.dto;

import com.wire.wirebarley.domain.Transaction;
import com.wire.wirebarley.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String accountNumber,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String targetAccountNumber,
        BigDecimal fee,
        String description,
        LocalDateTime createdAt
) {
    public TransactionResponse(Transaction transaction) {
        this(
                transaction.getId(),
                transaction.getAccountNumber(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getTargetAccountNumber(),
                transaction.getFee(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
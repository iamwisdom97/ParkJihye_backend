package com.wire.wirebarley.dto;

import com.wire.wirebarley.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        LocalDateTime createdAt
) {
    public AccountResponse(Account account) {
        this(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}
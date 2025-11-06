package com.wire.wirebarley.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal transferAmount,
        BigDecimal fee,
        BigDecimal totalDeduction,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceAfter,
        LocalDateTime transferredAt
) {
    public TransferResponse(String sourceAccountNumber, String targetAccountNumber,
                            BigDecimal transferAmount, BigDecimal fee,
                            BigDecimal sourceBalanceAfter, BigDecimal targetBalanceAfter) {
        this(
                sourceAccountNumber,
                targetAccountNumber,
                transferAmount,
                fee,
                transferAmount.add(fee),
                sourceBalanceAfter,
                targetBalanceAfter,
                LocalDateTime.now()
        );
    }
}
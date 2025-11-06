package com.wire.wirebarley.domain;

public enum TransactionType {
    DEPOSIT,        // 입금
    WITHDRAWAL,     // 출금
    TRANSFER_OUT,   // 이체 출금
    TRANSFER_IN,    // 이체 입금
    FEE            // 수수료
}
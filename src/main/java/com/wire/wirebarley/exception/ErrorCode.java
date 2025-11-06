package com.wire.wirebarley.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Account errors
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "Account not found"),
    ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "A002", "Account already exists"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "A003", "Insufficient balance"),

    // Transaction errors
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "T001", "Invalid amount"),
    SAME_ACCOUNT_TRANSFER(HttpStatus.BAD_REQUEST, "T002", "Cannot transfer to the same account"),

    // Daily limit errors
    DAILY_WITHDRAWAL_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "L001", "Daily withdrawal limit exceeded (max: 1,000,000)"),
    DAILY_TRANSFER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "L002", "Daily transfer limit exceeded (max: 3,000,000)"),

    // System errors
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "S001", "Concurrent update detected. Please try again"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "Internal server error");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
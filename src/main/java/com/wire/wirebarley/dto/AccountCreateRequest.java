package com.wire.wirebarley.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountCreateRequest(
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "^[0-9]{10,20}$", message = "Account number must be 10-20 digits")
        String accountNumber
) {
}
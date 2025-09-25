package com.clyrafy.wallet.ledger.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LedgerFeeRequest {
    @NotNull(message = "Source wallet ID is required")
    private UUID sourceWalletId;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Fee description is required")
    private String description; // "Transaction fee", "Service charge", etc.

    @NotNull(message = "Related transaction ID is required")
    private UUID relatedTransactionId; // The transaction this fee is for
}
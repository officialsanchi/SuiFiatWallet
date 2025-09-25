package com.clyrafy.wallet.ledger.dtos.requests;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LedgerDepositRequest {
    @NotNull(message = "Destination wallet ID is required")
    private UUID destinationWalletId;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "External reference is required")
    private String externalProviderReference;

    private String description;
}
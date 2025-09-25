 package com.clyrafy.wallet.transaction.dtos.request;

import com.clyrafy.wallet.wallet.enums.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuiDepositRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "walletType is required")
    private WalletType walletType = WalletType.SUI;

    private String memo;

    private String txHash;

    private String senderAddress;
}
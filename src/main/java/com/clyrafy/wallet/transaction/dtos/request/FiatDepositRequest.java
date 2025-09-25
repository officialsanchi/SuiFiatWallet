package com.clyrafy.wallet.transaction.dtos.request;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiatDepositRequest {
    private BigDecimal amount;                 // Deposit amount
    private WalletType walletType;             // Should be NGN for Paystack deposits
    private String recipientIdentifier;        // Can be email, username, or virtual account
    private String callbackUrl;                // Optional: for Paystack redirect
    private String email;                // Optional: for Paystack redirect
}

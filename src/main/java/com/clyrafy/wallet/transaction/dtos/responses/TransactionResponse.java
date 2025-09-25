package com.clyrafy.wallet.transaction.dtos.responses;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TransactionResponse {

    private String reference;
    private String status;
    private String txHash;
    private BigDecimal amount;
    private String sender;
    private String recipient;
    private BigDecimal senderBalance;  // New field for sender's balance
    private BigDecimal recipientBalance;  // New field for recipient's balance
    private WalletType type;  // Assuming WalletType is an enum, add this if missing
    private String message;
    private String channel;

}
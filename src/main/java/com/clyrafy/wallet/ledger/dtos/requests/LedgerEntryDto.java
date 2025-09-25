package com.clyrafy.wallet.ledger.dtos.requests;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryDto {
    private UUID walletId;
    private WalletType walletType;
    private BigDecimal amount;
    private String receiverAddress;
    private String ownerType;
    private String txHash;
    private String channel;
    private UUID initiatorId;
}

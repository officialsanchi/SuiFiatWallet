package com.clyrafy.wallet.wallet.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class UpdateWalletRequest {
    private UUID walletId;
    private BigDecimal fiatAmount;
    private BigDecimal suiAmount;
}

package com.clyrafy.wallet.wallet.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
    private String walletId;
    private String virtualAccountNum;
    private String walletType;
    private BigDecimal balance;
}

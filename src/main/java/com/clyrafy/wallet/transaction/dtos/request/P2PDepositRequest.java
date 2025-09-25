package com.clyrafy.wallet.transaction.dtos.request;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class P2PDepositRequest {
    private String senderWalletValue;
    private String receiverWalletValue;
    private BigDecimal amount;
    private WalletType walletType;
}

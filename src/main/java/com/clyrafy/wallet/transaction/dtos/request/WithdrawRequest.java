package com.clyrafy.wallet.transaction.dtos.request;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WithdrawRequest {
    private String value;
    private BigDecimal amount;
    private String reason;
    private String recipientCode;
    private WalletType walletType;
    private String receiverAddress;
}

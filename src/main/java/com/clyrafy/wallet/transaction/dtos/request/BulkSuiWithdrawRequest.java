package com.clyrafy.wallet.transaction.dtos.request;

import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkSuiWithdrawRequest {
    private String senderWalletValue;
    private List<SuiWithdrawRequest> requests;
    private WalletType type;
}

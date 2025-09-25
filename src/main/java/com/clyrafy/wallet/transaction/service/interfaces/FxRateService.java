package com.clyrafy.wallet.transaction.service.interfaces;

import com.clyrafy.wallet.wallet.enums.WalletType;
import java.math.BigDecimal;

public interface FxRateService {
    BigDecimal getSuiToNgnRate();
    BigDecimal getUsdToNgnRate();
    BigDecimal convertToFiat(BigDecimal amount, WalletType walletType);
    BigDecimal convertToSui(BigDecimal amount, WalletType walletType);
}

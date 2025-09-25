package com.clyrafy.wallet.transaction.service.interfaces;

import com.clyrafy.wallet.transaction.dtos.request.FiatWithdrawRequest;
import com.clyrafy.wallet.transaction.dtos.request.SuiWithdrawRequest;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;

import java.util.UUID;

public interface WithdrawalService {
    TransactionResponse withdrawToFiat(FiatWithdrawRequest request, UUID walletId, UUID initiatorId, String initiatorType);
    TransactionResponse withdrawToSui(SuiWithdrawRequest request, UUID walletId, UUID initiatorId, String initiatorType);

}

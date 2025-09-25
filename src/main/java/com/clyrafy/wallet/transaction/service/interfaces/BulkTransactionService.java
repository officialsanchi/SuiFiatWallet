package com.clyrafy.wallet.transaction.service.interfaces;

import com.clyrafy.wallet.transaction.dtos.request.*;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.wallet.data.models.Wallet;

import java.util.List;

public interface BulkTransactionService {
    List<TransactionResponse> disburseBulkFiatDeposit(Wallet senderWallet, List<FiatDepositRequest> requests);

    List<TransactionResponse> disburseBulkSuiDeposit(Wallet senderWallet, List<SuiDepositRequest> requests);

    List<TransactionResponse> disburseBulkFiatWithdraw(Wallet senderWallet, List<FiatWithdrawRequest> requests);

    List<TransactionResponse> disburseBulkSuiWithdraw(Wallet senderWallet, List<SuiWithdrawRequest> requests);
}

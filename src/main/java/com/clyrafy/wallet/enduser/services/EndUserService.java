package com.clyrafy.wallet.enduser.services;

import com.clyrafy.wallet.enduser.dtos.requests.UpdateEndUserDetailsRequest;
import com.clyrafy.wallet.enduser.dtos.responses.UpdateEndUserDetailsResponse;
import com.clyrafy.wallet.transaction.dtos.request.FiatDepositRequest;
import com.clyrafy.wallet.transaction.dtos.request.P2PDepositRequest;
import com.clyrafy.wallet.transaction.dtos.request.SuiDepositRequest;
import com.clyrafy.wallet.transaction.dtos.request.WithdrawRequest;
import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EndUserService {


    TransactionResponse endUserFiatDeposit(FiatDepositRequest request);
//    TransactionResponse endUserSuiDeposit(SuiDepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request);
    TransactionResponse endUserP2PFiatDeposit(P2PDepositRequest request);
//    TransactionResponse endUserP2PSuiDeposit(P2PDepositRequest request);
    BalanceResponse checkBalance(UUID endUserId, WalletType type);
    List<TransactionResponse> getTransactionHistory(UUID endUserId);
    Wallet linkWalletToOrganization(UUID endUserId, UUID organizationId);

    @Transactional
    UpdateEndUserDetailsResponse updateEndUserDetails(UpdateEndUserDetailsRequest request);
}

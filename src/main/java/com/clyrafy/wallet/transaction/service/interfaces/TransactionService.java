package com.clyrafy.wallet.transaction.service.interfaces;

import com.clyrafy.wallet.transaction.dtos.request.*;
import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionSummaryResponse;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    @PreAuthorize("isAuthenticated()")
    BalanceResponse checkFiatBalance();

    @PreAuthorize("isAuthenticated()")
    BalanceResponse suiCheckBalance();

    @PreAuthorize("isAuthenticated()")
    TransactionResponse fiatDeposit(FiatDepositRequest request);

    @PreAuthorize("isAuthenticated()")
    TransactionResponse p2pFiatDeposit(P2PDepositRequest request);

    @PreAuthorize("isAuthenticated()")
    TransactionResponse p2pSuiDeposit(P2PDepositRequest request);

    @PreAuthorize("isAuthenticated()")
    TransactionResponse fiatWithdraw(FiatWithdrawRequest request);

    @PreAuthorize("isAuthenticated()")
    TransactionResponse suiWithdraw(SuiWithdrawRequest request);

    @PreAuthorize("isAuthenticated()")
    List<TransactionResponse> getTransactionHistory();

    @PreAuthorize("hasRole('ORG_ADMIN')")
    List<TransactionSummaryResponse> getOrganizationTransactionSummaries();

    List<TransactionResponse> getTransactionHistoryForWallets(List<Wallet> wallets);

    void handlePaystackWebhook(String payload);
//    @PreAuthorize("isAuthenticated()")
//    TransactionResponse fiatDeposit(FiatDepositRequest request);
//
////    @PreAuthorize("isAuthenticated()")
////    TransactionResponse suiDeposit(@Valid SuiDepositRequest request);
//
//    @PreAuthorize("isAuthenticated()")
//    TransactionResponse fiatWithdraw(FiatWithdrawRequest request);
//
//    @PreAuthorize("isAuthenticated()")
//    TransactionResponse suiWithdraw(SuiWithdrawRequest request);
//
//    TransactionResponse p2pFiatDeposit(P2PDepositRequest request);
//
//    TransactionResponse p2pSuiDeposit(P2PDepositRequest request);
//
//    @Transactional
//    void updateWalletBalance(Wallet wallet, com.clyrafy.wallet.wallet.enums.WalletType currency, BigDecimal amount);
//
//    @PreAuthorize("isAuthenticated()")
//    BalanceResponse checkFiatBalance();
//
//    @PreAuthorize("isAuthenticated()")
//    BalanceResponse suiCheckBalance();
//
//    @PreAuthorize("isAuthenticated()")
//    List<TransactionResponse> getTransactionHistory();
//
//    @PreAuthorize("hasRole('ORG_ADMIN')")
//    List<TransactionSummaryResponse> getOrganizationTransactionSummaries();
//
//    void confirmSuiDeposit(@Valid SuiDepositRequest request);
//
//    List<TransactionResponse> getTransactionHistoryForWallets(List<Wallet> wallets);
//    List<TransactionResponse> bulkFiatDeposit(BulkFiatDepositRequest request);
//    List<TransactionResponse> bulkSuiDeposit(BulkSuiDepositRequest request);

//    List<TransactionResponse> bulkFiatWithdraw(BulkFiatWithdrawRequest request);
//    List<TransactionResponse> bulkSuiWithdraw(BulkSuiWithdrawRequest request);

}

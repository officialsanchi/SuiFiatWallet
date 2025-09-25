package com.clyrafy.wallet.transaction.controller;

import com.clyrafy.wallet.transaction.dtos.request.*;
import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionSummaryResponse;
import com.clyrafy.wallet.transaction.service.implementation.PaystackService;
import com.clyrafy.wallet.transaction.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final PaystackService paystackService;

    @GetMapping("/balance/fiat")
    @PreAuthorize("hasRole('END_USER') or hasRole('ORG_ADMIN')")
    public ResponseEntity<BalanceResponse> checkFiatBalance() {
        return ResponseEntity.ok(transactionService.checkFiatBalance());
    }

    @GetMapping("/balance/sui")
    @PreAuthorize("hasRole('END_USER') or hasRole('ORG_ADMIN')")
    public ResponseEntity<BalanceResponse> checkSuiBalance() {
        return ResponseEntity.ok(transactionService.suiCheckBalance());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('END_USER') or hasRole('ORG_ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory() {
        return ResponseEntity.ok(transactionService.getTransactionHistory());
    }

    @PostMapping("/deposit/fiat")
    @PreAuthorize("hasRole('END_USER') or hasRole('ORG_ADMIN')")
    public ResponseEntity<TransactionResponse> fiatDeposit(@RequestBody FiatDepositRequest request) {
        return ResponseEntity.ok(transactionService.fiatDeposit(request));
    }
//    public ResponseEntity<PaystackDepositResponse> depositFiat(@RequestBody BigDecimal amount, String recipientEmail, String reference,
//                                                               String businessId, String userId, String walletId, String callbackUrl){

//        public ResponseEntity<PaystackDepositResponse> depositFiat(@RequestBody DepositDto depositDto){
//        return ResponseEntity.ok( paystackService.deposit(depositDto.getAmount(), depositDto.getRecipientEmail(), "reference", "businessId", "userId", "walletId", ""));

//    }

//    @PostMapping("/deposit/sui")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<TransactionResponse> suiDeposit(@RequestBody SuiDepositRequest request) {
//        return ResponseEntity.ok(transactionService.suiDeposit(request));
//    }

    @PostMapping("/withdraw/fiat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> fiatWithdraw(@RequestBody FiatWithdrawRequest request) {
        return ResponseEntity.ok(transactionService.fiatWithdraw(request));
    }

    @PostMapping("/withdraw/sui")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> suiWithdraw(@RequestBody SuiWithdrawRequest request) {
        return ResponseEntity.ok(transactionService.suiWithdraw(request));
    }

    @GetMapping("/organization/summaries")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<List<TransactionSummaryResponse>> getOrganizationTransactionSummaries() {
        return ResponseEntity.ok(transactionService.getOrganizationTransactionSummaries());
    }

}

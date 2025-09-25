package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.ledger.dtos.requests.LedgerEntryDto;
import com.clyrafy.wallet.ledger.service.LedgerService;
import com.clyrafy.wallet.transaction.dtos.request.FiatWithdrawRequest;
import com.clyrafy.wallet.transaction.dtos.request.SuiWithdrawRequest;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.transaction.service.interfaces.FxRateService;
import com.clyrafy.wallet.transaction.service.interfaces.WithdrawalService;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalServiceImpl implements WithdrawalService {

    private final RestTemplate restTemplate;
    private final LedgerService ledgerService;
    private final WalletService walletService;
    private final FxRateService fxRateService;

    @Value("${withdraw.sui.endpoint}")
    private String suiWithdrawalServiceUrl;

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;


    @Override
    public TransactionResponse withdrawToFiat(FiatWithdrawRequest request, UUID walletId, UUID initiatorId, String initiatorType) {
        BigDecimal fiatAmount = fxRateService.convertToFiat(request.getAmount(), request.getWalletType());

        Map<String, Object> payloadForPaystack = Map.of(
                "source", "balance",
                "reason", request.getReason(),
                "amount", fiatAmount.multiply(BigDecimal.valueOf(100)).intValue(),
                "recipient", request.getRecipientCode()
        );

        try {
            ResponseEntity<Map> response = sendRequest(
                    "https://api.paystack.co/transfer",
                    payloadForPaystack,
                    true
            );

            Map<String, Object> body = response.getBody();
            boolean success = Boolean.TRUE.equals(body.get("status"));
            String reference = (String) ((Map<String, Object>) body.get("data")).get("reference");

            return buildResponse(walletId, request.getReceiverAddress(), request.getWalletType(), fiatAmount,
                    success ? "SUCCESS" : "FAILED",
                    success ? "Fiat withdrawal processed" : "Fiat withdrawal failed",
                    reference, null, "FIAT", initiatorId, initiatorType);

        } catch (Exception e) {
            log.error("Fiat withdrawal failed", e);
            return buildErrorResponse(request.getReceiverAddress(), request.getAmount(), "FIAT", e);
        }
    }

    @Override
    public TransactionResponse withdrawToSui(SuiWithdrawRequest request, UUID walletId, UUID initiatorId, String initiatorType) {
        BigDecimal suiAmount = fxRateService.convertToSui(request.getAmount(), request.getWalletType());

        Map<String, Object> payload = Map.of(
                "recipientAddress", request.getReceiverAddress(),
                "amount", suiAmount.toPlainString(),
                "reason", request.getReason()
        );

        try {
            ResponseEntity<Map> response = sendRequest(
                    suiWithdrawalServiceUrl,
                    payload,
                    false
            );

            String txHash = (String) response.getBody().get("txHash");

            return buildResponse(walletId, request.getReceiverAddress(), WalletType.SUI, suiAmount,
                    "SUCCESS", "Sui withdrawal successful",
                    UUID.randomUUID().toString(), txHash, "SUI", initiatorId, initiatorType);

        } catch (Exception e) {
            log.error("Sui withdrawal failed", e);
            return buildErrorResponse(request.getReceiverAddress(), request.getAmount(), "SUI", e);
        }
    }



    private ResponseEntity<Map> sendRequest(String url, Map<String, Object> payload, boolean withPaystackAuth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (withPaystackAuth) {
            headers.setBearerAuth(paystackSecretKey);
        }
        return restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Map.class);
    }

    private TransactionResponse buildResponse(UUID walletId, String receiver, WalletType walletType, BigDecimal amount,
                                              String status, String message, String reference, String txHash,
                                              String channel, UUID initiatorId, String initiatorType) {

        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        String ownerType = resolveOwnerType(wallet);

        ledgerService.recordWithdrawal(LedgerEntryDto.builder()
                .walletId(walletId)
                .walletType(walletType)
                .amount(amount)
                .receiverAddress(receiver)
                .ownerType(ownerType)
                .initiatorId(initiatorId)
                .txHash(txHash)
                .channel(channel)
                .build()
        );

        return TransactionResponse.builder()
                .reference(reference)
                .status(status)
                .message(message)
                .txHash(txHash)
                .amount(amount)
//                .(walletType == WalletType.SUI ? fxRateService.convertToFiat(amount, WalletType.SUI) : null)
//                .balance(walletService.getBalance(initiatorId, walletType))
                .recipient(receiver)
                .channel(channel)
                .build();
    }


    private TransactionResponse buildErrorResponse(String receiver, BigDecimal amount, String channel, Exception exception) {
        return TransactionResponse.builder()
                .reference(UUID.randomUUID().toString())
                .status("FAILED")
                .message("Withdrawal failed: " + exception.getMessage())
                .amount(amount)
                .recipient(receiver)
                .channel(channel)
                .build();
    }

    private String resolveOwnerType(Wallet wallet) {
        if (wallet.getEndUser() != null) return "END_USER";
        if (wallet.getOrganization() != null) return "ORG_USER";
        return "UNKNOWN";
    }

}

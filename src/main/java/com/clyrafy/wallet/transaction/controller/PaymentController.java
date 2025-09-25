package com.clyrafy.wallet.transaction.controller;

import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
import com.clyrafy.wallet.transaction.service.implementation.PaystackService;
import com.clyrafy.wallet.transaction.service.implementation.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final TransactionServiceImpl transactionService;
    private final PaystackService paystackService;
    private final String webhookSecret;

    public PaymentController(TransactionServiceImpl transactionService, PaystackService paystackService,
                             @Value("${paystack.secret-key}") String webhookSecret) {
        this.transactionService = transactionService;
        this.paystackService = paystackService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "x-paystack-signature", required = false) String signatureHeader) {
        if (signatureHeader == null) {
            return ResponseEntity.status(400).body("Missing signature header");
        }

        if (!verifySignature(payload, webhookSecret, signatureHeader)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        transactionService.handlePaystackWebhook(payload);
        return ResponseEntity.ok("Received");
    }

    private boolean verifySignature(String payload, String secret, String signatureHeader) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = Arrays.toString(Hex.encode(digest));
            return computed.equalsIgnoreCase(signatureHeader);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @PostMapping("/deposit")
    public ResponseEntity<PaystackDepositResponse> depositFiat(@RequestBody BigDecimal amount, String recipientEmail, String reference,
                                                               String businessId, String userId, String walletId, String callbackUrl){
        return ResponseEntity.ok( paystackService.deposit(amount, recipientEmail, reference, businessId, userId, walletId, callbackUrl));

    }
}
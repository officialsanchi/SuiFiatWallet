package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PaystackService {

    private final RestTemplate restTemplate;

    public PaystackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Value("${paystack.base-url}")
    private String baseUrl;

    private static final String SUI_BASE_URL = "https://glass-wallet-listener.onrender.com/api";

    @PostConstruct
    public void init() {
        System.out.println("Paystack Secret Key = " + secretKey);
    }

    /**
     * Initialize a Paystack deposit.
     *
     * @param amount         Amount in NGN
     * @param recipientEmail Email of the recipient
     * @param reference      Unique reference for transaction
     * @param businessId     Optional business ID
     * @param userId         Optional user ID
     * @param walletId       Optional wallet ID
     * @param callbackUrl    Optional callback URL
     * @return PaystackDepositResponse with authorization URL and reference
     */
    public PaystackDepositResponse deposit(BigDecimal amount, String recipientEmail, String reference,
                                           String businessId, String userId, String walletId, String callbackUrl) {

        String url = baseUrl + "/transaction/initialize";
        log.info("Initializing Paystack deposit at URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        log.info("EMail is {}, amount is {}", recipientEmail, amount);
        payload.put("email", recipientEmail);
        payload.put("amount", amount);
        payload.put("reference", reference);
        payload.put("currency", "NGN");

        if (callbackUrl != null && !callbackUrl.isBlank()) {
            payload.put("callback_url", callbackUrl);
            log.info("Callback URL set: {}", callbackUrl);
        }

        payload.put("channels", new String[]{"bank_transfer"});

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("business_id", businessId);
        metadata.put("user_id", userId);
        metadata.put("wallet_id", walletId);
        payload.put("metadata", metadata);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("Paystack response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                boolean status = Boolean.parseBoolean(body.get("status").toString());
                log.info("Paystack response body status: {}", status);

                if (status) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    log.info("Paystack deposit data: {}", data);

                    return PaystackDepositResponse.builder()
                            .success(true)
                            .authorizationUrl((String) data.get("authorization_url"))
                            .accessCode((String) data.get("access_code"))
                            .reference((String) data.get("reference"))
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error initializing Paystack deposit", e);
        }

        return PaystackDepositResponse.builder()
                .success(false)
                .reference(reference)
                .build();
    }


    /**
     * Verify a Paystack transaction.
     */
    public boolean verifyDeposit(String reference) {
        String url = baseUrl + "/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return data != null && "success".equalsIgnoreCase((String) data.get("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Initiate a Paystack withdrawal.
     */
    public boolean withdraw(BigDecimal amount, String recipientCode, String reason) {
        String url = baseUrl + "/transfer";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("source", "balance");
        payload.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        payload.put("recipient", recipientCode);
        payload.put("reason", reason);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return data != null && "success".equalsIgnoreCase((String) data.get("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Withdraw SUI coins via external service.
     */
    public TransactionResponse withdrawSui(String sender, String recipient, BigDecimal amount) {
        String url = SUI_BASE_URL + "/withdrawSuiCoin";

        Map<String, Object> payload = new HashMap<>();
        payload.put("senderAddress", sender);
        payload.put("recipientAddress", recipient);
        payload.put("amount", amount);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.getOrDefault("status", "FAILED");
                String message = (String) body.getOrDefault("message", "");
                String txHash = (String) body.getOrDefault("transactionHash", null);

                return TransactionResponse.builder()
                        .status(status.toUpperCase())
                        .message(message)
                        .txHash(txHash)
                        .amount(amount)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionResponse.builder()
                .status("FAILED")
                .message("SUI withdrawal failed")
                .amount(amount)
                .build();
    }

    /**
     * Check Paystack balance.
     */
    public Double checkBalance() {
        String url = baseUrl + "/balance";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                if (!data.isEmpty()) {
                    Integer balanceInKobo = (Integer) data.get(0).get("balance");
                    return balanceInKobo / 100.0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}

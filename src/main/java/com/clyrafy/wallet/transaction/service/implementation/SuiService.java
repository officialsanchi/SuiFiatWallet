//package com.clyrafy.wallet.transaction.service.implementation;
//
//import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
//import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class SuiService {
//
//    @Value("${paystack.secret-key}")
//    private String secretKey;
//
//    @Value("${BASEURL}")
//    private String baseUrl;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    private static final String SUI_BASE_URL = "https://glass-wallet-listener.onrender.com/api";
//
//    @PostConstruct
//    public void init() {
//        System.out.println("Paystack Secret Key = " + secretKey);
//    }
//
//
//
//    public TransactionResponse withdrawSui(String sender, String recipient, double amount) {
//        String url = SUI_BASE_URL + "/withdrawSuiCoin";
//        Map<String, Object> request = Map.of(
//                "senderAddress", sender,
//                "recipientAddress", recipient,
//                "amount", amount
//        );
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
//
//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            Map<String, Object> body = response.getBody();
//            String status = (String) body.get("status");
//            String message = (String) body.get("message");
//            String txHash = (String) body.get("transactionHash");
//
//            TransactionResponse.builder()
//                    .status(status)
//                    .message(message)
//                    .txHash(txHash)
//                    .build();
//        }
//        return new TransactionResponse();
//    }
//
//    private ResponseEntity<Map> postForMap(String url, Map<String, Object> payload) {
//        HttpHeaders headers = createHeaders();
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
//        return restTemplate.postForEntity(url, request, Map.class);
//    }
//
//    private ResponseEntity<Map> getForMap(String url) {
//        HttpHeaders headers = createHeaders();
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//        return restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
//    }
//
//    private HttpHeaders createHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(secretKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//}

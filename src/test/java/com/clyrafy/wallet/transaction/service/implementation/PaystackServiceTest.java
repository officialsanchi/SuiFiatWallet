//package com.clyrafy.wallet.transaction.service.implementation;
//
//import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
//import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//class PaystackServiceTest {
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @InjectMocks
//    private PaystackService paystackService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        paystackService = new PaystackService(restTemplate);
//        paystackService.secretKey = "sk_test_6d4411ab4887dce0e39133066ad7130ca1d85c69";
//        paystackService.baseUrl = "https://api.paystack.co";
//    }
//
//    @Test
//    void deposit_shouldReturnSuccessResponse() {
//        BigDecimal amount = BigDecimal.valueOf(1000);
//        String reference = "ref123";
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("authorization_url", "http://paystack.com/auth");
//        data.put("access_code", "AC123");
//        data.put("reference", reference);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("status", true);
//        body.put("data", data);
//
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        PaystackDepositResponse response = paystackService.deposit(amount, "user@example.com", reference);
//
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getAuthorizationUrl()).isEqualTo("http://paystack.com/auth");
//        assertThat(response.getReference()).isEqualTo(reference);
//    }
//
//    @Test
//    void deposit_shouldReturnFailedResponseWhenApiFails() {
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
//
//        PaystackDepositResponse response = paystackService.deposit(BigDecimal.TEN, "user@example.com", "ref123");
//
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getReference()).isEqualTo("ref123");
//    }
//
//    @Test
//    void verifyDeposit_shouldReturnTrueWhenStatusSuccess() {
//        Map<String, Object> data = new HashMap<>();
//        data.put("status", "success");
//        Map<String, Object> body = Map.of("data", data);
//
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        boolean result = paystackService.verifyDeposit("ref123");
//
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void verifyDeposit_shouldReturnFalseWhenApiFails() {
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
//
//        boolean result = paystackService.verifyDeposit("ref123");
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    void withdraw_shouldReturnTrueWhenStatusSuccess() {
//        Map<String, Object> data = Map.of("status", "success");
//        Map<String, Object> body = Map.of("data", data);
//
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        boolean result = paystackService.withdraw(BigDecimal.valueOf(500), "recipient123", "test reason");
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void withdraw_shouldReturnFalseWhenApiFails() {
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
//
//        boolean result = paystackService.withdraw(BigDecimal.valueOf(500), "recipient123", "test reason");
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    void withdrawSui_shouldReturnSuccessResponse() {
//        Map<String, Object> body = new HashMap<>();
//        body.put("status", "success");
//        body.put("message", "Transfer complete");
//        body.put("transactionHash", "0xabc123");
//
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        TransactionResponse response = paystackService.withdrawSui("sender", "recipient", BigDecimal.TEN);
//
//        assertThat(response.getStatus()).isEqualTo("SUCCESS");
//        assertThat(response.getMessage()).isEqualTo("Transfer complete");
//        assertThat(response.getTxHash()).isEqualTo("0xabc123");
//    }
//
//    @Test
//    void withdrawSui_shouldReturnFailedResponseWhenApiFails() {
//        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
//
//        TransactionResponse response = paystackService.withdrawSui("sender", "recipient", BigDecimal.TEN);
//
//        assertThat(response.getStatus()).isEqualTo("FAILED");
//        assertThat(response.getMessage()).isEqualTo("SUI withdrawal failed");
//    }
//
//    @Test
//    void checkBalance_shouldReturnBalanceWhenAvailable() {
//        Map<String, Object> wallet = Map.of("balance", 50000); // 500 Naira
//        List<Map<String, Object>> data = List.of(wallet);
//        Map<String, Object> body = Map.of("data", data);
//
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        Double balance = paystackService.checkBalance();
//
//        assertThat(balance).isEqualTo(500.0);
//    }
//
//    @Test
//    void checkBalance_shouldReturnZeroWhenNoData() {
//        Map<String, Object> body = Map.of("data", List.of());
//
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
//                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
//
//        Double balance = paystackService.checkBalance();
//
//        assertThat(balance).isEqualTo(0.0);
//    }
//}

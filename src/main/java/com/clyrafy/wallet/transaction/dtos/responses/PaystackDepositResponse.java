package com.clyrafy.wallet.transaction.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaystackDepositResponse {
    private boolean success;
    private String authorizationUrl;
    private String accessCode;
    private String reference;
}

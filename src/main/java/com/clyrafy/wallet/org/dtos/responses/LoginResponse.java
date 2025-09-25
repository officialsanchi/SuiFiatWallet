package com.clyrafy.wallet.org.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String role;
    private String message;
    private String orgId;
}

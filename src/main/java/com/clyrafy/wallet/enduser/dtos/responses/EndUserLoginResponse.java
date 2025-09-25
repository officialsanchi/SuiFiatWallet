package com.clyrafy.wallet.enduser.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndUserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String fullName;
    private UUID organizationId;
    private String message;
}

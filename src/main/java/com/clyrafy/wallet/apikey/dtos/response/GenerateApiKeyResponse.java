package com.clyrafy.wallet.apikey.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateApiKeyResponse {
    private String apiKeyId;
    private String publicKey;
    private String secretKey;
    private String type;
    private String organizationId;
}

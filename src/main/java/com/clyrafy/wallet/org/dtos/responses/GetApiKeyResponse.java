package com.clyrafy.wallet.org.dtos.responses;

import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GetApiKeyResponse {
    private  String apiKeyId;
    private String publicKey;
    private String secretKey;
    private ApiKeyType type;
    private String orgId;
}

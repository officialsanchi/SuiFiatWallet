package com.clyrafy.wallet.apikey.dtos.requests;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateApiKeyRequest {
    private String organizationId;
    private String environment;
}

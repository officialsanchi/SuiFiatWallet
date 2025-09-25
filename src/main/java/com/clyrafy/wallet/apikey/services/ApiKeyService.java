package com.clyrafy.wallet.apikey.services;

import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.apikey.dtos.requests.GenerateApiKeyRequest;
import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    GenerateApiKeyResponse generateApiKeys(GenerateApiKeyRequest request);
    GenerateApiKeyResponse rotateApiKeys(UUID organizationId, ApiKeyType type);
    void revokeApiKey(UUID apiKeyId);
    List<GenerateApiKeyResponse> getAllOrganizationApiKeys(UUID organizationId, ApiKeyType type);
    GenerateApiKeyResponse switchToLive(UUID orgId);
}


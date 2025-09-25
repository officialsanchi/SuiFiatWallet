package com.clyrafy.wallet.apikey.services.impl;

import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.apikey.data.repositories.ApiKeyRepository;
import com.clyrafy.wallet.apikey.dtos.requests.GenerateApiKeyRequest;
import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyStatus;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.apikey.services.ApiKeyService;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ApiServiceImpl implements ApiKeyService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    private final ApiKeyRepository apiKeyRepository;
    private final OrganizationRespository organizationRepository;

    public ApiServiceImpl(ApiKeyRepository apiKeyRepository, OrganizationRespository organizationRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public GenerateApiKeyResponse generateApiKeys(GenerateApiKeyRequest request) {
        UUID orgId = UUID.fromString(request.getOrganizationId());
        ApiKeyType type = ApiKeyType.valueOf(request.getEnvironment());
        return createNewApiKey(orgId, type, type == ApiKeyType.SANDBOX ? 10 : Integer.MAX_VALUE);
    }

    @Override
    public GenerateApiKeyResponse rotateApiKeys(UUID organizationId, ApiKeyType type) {
        return createNewApiKey(organizationId, type, type == ApiKeyType.SANDBOX ? 10 : Integer.MAX_VALUE);
    }

    @Override
    public void revokeApiKey(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found"));
        apiKey.setStatus(ApiKeyStatus.REVOKED);
        apiKey.setExpiredAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
    }

    @Override
    public List<GenerateApiKeyResponse> getAllOrganizationApiKeys(UUID organizationId, ApiKeyType type) {
        var org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        if (type != null) {
            return apiKeyRepository.findByOrganizationIdAndTypeAndStatus(org.getId(), type, ApiKeyStatus.ACTIVE)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return apiKeyRepository.findByOrganizationId(org.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public GenerateApiKeyResponse switchToLive(UUID orgId) {
        return apiKeyRepository.findByOrganizationIdAndTypeAndStatus(orgId, ApiKeyType.LIVE, ApiKeyStatus.ACTIVE)
                .stream()
                .findFirst()
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    inactivateExistingKeysBatch(orgId, ApiKeyType.SANDBOX);
                    return createNewApiKey(orgId, ApiKeyType.LIVE, Integer.MAX_VALUE);
                });
    }


    private GenerateApiKeyResponse createNewApiKey(UUID organizationId, ApiKeyType type, int sandboxLimit) {
        var org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        enforceSandboxLimit(org.getId(), type, sandboxLimit);
        inactivateExistingKeysBatch(org.getId(), type);

        String publicKeyRaw = generateRandomKey(24);
        String secretKeyRaw = generateRandomKey(48);

        String publicKey = (type == ApiKeyType.SANDBOX ? "pk_test_" : "pk_live_") + publicKeyRaw;
        String secretKey = (type == ApiKeyType.SANDBOX ? "sk_test_" : "sk_live_") + secretKeyRaw;

        ApiKey newKey = ApiKey.builder()
                .publicKey(publicKey)
                .secretKey(secretKey)
                .type(type)
                .organization(org)
                .status(ApiKeyStatus.ACTIVE)
                .build();

        ApiKey saved = apiKeyRepository.save(newKey);
        return mapToResponse(saved);
    }

    private void enforceSandboxLimit(UUID orgId, ApiKeyType type, int limit) {
        if (type == ApiKeyType.SANDBOX) {
            long activeCount = apiKeyRepository.countByOrganizationIdAndTypeAndStatus(orgId, type, ApiKeyStatus.ACTIVE);
            if (activeCount >= limit) {
                throw new IllegalStateException("Maximum number of active sandbox API keys reached");
            }
        }
    }

//    @Transactional
    private void inactivateExistingKeysBatch(UUID orgId, ApiKeyType type) {
        LocalDateTime now = LocalDateTime.now();
        apiKeyRepository.inactivateKeys(orgId, type, now);
    }

    private String generateRandomKey(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private GenerateApiKeyResponse mapToResponse(ApiKey apiKey) {
        return GenerateApiKeyResponse.builder()
                .apiKeyId(apiKey.getId().toString())
                .publicKey(apiKey.getPublicKey())
                .secretKey(apiKey.getSecretKey())
                .type(apiKey.getType().name())
                .organizationId(apiKey.getOrganization().getId().toString())
                .build();
    }
}

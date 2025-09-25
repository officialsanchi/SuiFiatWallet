package com.clyrafy.wallet.org.controllers;

import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.apikey.services.ApiKeyService;
import com.clyrafy.wallet.org.dtos.responses.GetApiKeyResponse;
import com.clyrafy.wallet.org.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/developer")
@RequiredArgsConstructor
public class DeveloperController {

    private final UserService userService;
    private final ApiKeyService apiKeyService;


    @GetMapping("/api-keys")
    public ResponseEntity<GetApiKeyResponse> getApiKeys(
            @RequestParam("environment") String environment
    ) {
        ApiKeyType type;
        try {
            type = ApiKeyType.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        GetApiKeyResponse response = userService.getApiKeys(type);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/api-keys/rotate")
    public ResponseEntity<GetApiKeyResponse> rotateApiKeys(
            @RequestParam("environment") String environment
    ) {
        ApiKeyType type;
        try {
            type = ApiKeyType.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        UUID orgId = userService.getCurrentUserOrganizationId();
        var apiKeyResponse = apiKeyService.rotateApiKeys(orgId, type);

        return ResponseEntity.ok(
                GetApiKeyResponse.builder()
                        .publicKey(apiKeyResponse.getPublicKey())
                        .secretKey(apiKeyResponse.getSecretKey())
                        .type(type)
                        .orgId(orgId.toString())
                        .build()
        );
    }


    @PostMapping("/api-keys/revoke")
    public ResponseEntity<Void> revokeApiKey(@RequestParam("apiKeyId") UUID apiKeyId) {
        apiKeyService.revokeApiKey(apiKeyId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/api-keys/all-keys")
    public ResponseEntity<List<GenerateApiKeyResponse>> getAllApiKeys(
            @RequestParam(value = "environment", required = false) String environment
    ) {
        UUID orgId = userService.getCurrentUserOrganizationId();

        if (environment != null) {
            ApiKeyType type;
            try {
                type = ApiKeyType.valueOf(environment.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(userService.fetchUserApiKeys(orgId, type));
        }

        return ResponseEntity.ok(userService.fetchUserApiKeys(orgId, null));
    }


    @PostMapping("/api-keys/switch")
    public ResponseEntity<GetApiKeyResponse> switchToLive() {
        UUID orgId = userService.getCurrentUserOrganizationId();
        var apiKeyResponse = apiKeyService.switchToLive(orgId);

        return ResponseEntity.ok(
                GetApiKeyResponse.builder()
                        .publicKey(apiKeyResponse.getPublicKey())
                        .secretKey(apiKeyResponse.getSecretKey())
                        .type(ApiKeyType.LIVE)
                        .orgId(orgId.toString())
                        .build()
        );
    }
}

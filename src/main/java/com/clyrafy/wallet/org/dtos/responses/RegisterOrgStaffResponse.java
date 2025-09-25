package com.clyrafy.wallet.org.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOrgStaffResponse {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String businessName;
    private String userName;
    private String userId;
    private String message;
    private String organizationId;
    private String virtualOrganizationId;
    private String apiPublicKey;
    private String apiSecret;
    private String KybStatus;
    private String apiKeyType;
    private String country;
    private String walletVirtualAccount;
}

package com.clyrafy.wallet.org.service;


import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.dtos.requests.LoginRequest;
import com.clyrafy.wallet.org.dtos.requests.RegisterOrgStaffRequest;
import com.clyrafy.wallet.org.dtos.responses.GetApiKeyResponse;
import com.clyrafy.wallet.org.dtos.responses.LoginResponse;
import com.clyrafy.wallet.org.dtos.responses.RegisterOrgStaffResponse;
import com.clyrafy.wallet.org.enums.EmailVerificationStatus;
import jakarta.transaction.Transactional;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface UserService {
    RegisterOrgStaffResponse registerOrgUser(RegisterOrgStaffRequest registerOrgStaffRequest);
    String resendVerificationEmail(String email);
    @Transactional
    EmailVerificationStatus verifyEmail(@NonNull String token);
    User getUserByEmail(String email);
    @Transactional
    String verifyEmailAndLogin(String token);
    LoginResponse loginUser(LoginRequest loginReq);
    GetApiKeyResponse getApiKeys(ApiKeyType environment);
    UUID getCurrentUserOrganizationId();
    List<GenerateApiKeyResponse> fetchUserApiKeys(UUID organizationId, ApiKeyType type);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    LoginResponse refreshAccessToken(String refreshToken);
    void logoutUser(@NonNull String refreshToken);
    User getUserById(UUID userId);
}

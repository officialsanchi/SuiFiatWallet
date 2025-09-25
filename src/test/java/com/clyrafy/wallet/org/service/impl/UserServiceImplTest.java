package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.apikey.data.repositories.ApiKeyRepository;
import com.clyrafy.wallet.apikey.dtos.requests.GenerateApiKeyRequest;
import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyStatus;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.apikey.services.ApiKeyService;
import com.clyrafy.wallet.ledger.service.LedgerService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.models.RefreshToken;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.*;
import com.clyrafy.wallet.org.dtos.requests.LoginRequest;
import com.clyrafy.wallet.org.dtos.requests.RegisterOrgStaffRequest;
import com.clyrafy.wallet.org.dtos.responses.LoginResponse;
import com.clyrafy.wallet.org.dtos.responses.RegisterOrgStaffResponse;
import com.clyrafy.wallet.org.enums.OrgStatus;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.org.service.EmailService;
import com.clyrafy.wallet.org.service.OrgCodeService;
import com.clyrafy.wallet.org.service.VerificationTokenService;
import com.clyrafy.wallet.security.JwtUtil;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRespository organizationRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private ApiKeyRepository apiKeyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private OrgCodeService orgCodeService;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private LedgerService ledgerService;


    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerOrgUser_shouldCreateUserAndOrganization() {
        RegisterOrgStaffRequest request = RegisterOrgStaffRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("pass123")
                .confirmPassword("pass123")
                .phoneNumber("12345")
                .businessName("Acme Inc")
                .country("US")
                .acceptTerms(true)
                .build();

        when(userRepository.findUserByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("12345")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("john@example.com");
        savedUser.setUserName("john");
        savedUser.setRole(Role.ORG_ADMIN);
        savedUser.setFullName("John Doe");
        savedUser.setEmailVerificationStatus(OrgStatus.PENDING_VERIFICATION);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName("Acme Inc");
        org.setKybStatus(com.clyrafy.wallet.kyb.enums.KybStatus.PENDING);
        when(organizationRepository.saveAndFlush(any(Organization.class))).thenReturn(org);

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        when(walletService.createWalletForOrganization(any(UUID.class), any(UUID.class), eq(WalletType.NGN)))
                .thenReturn(wallet);

        GenerateApiKeyResponse apiKeyResp = GenerateApiKeyResponse.builder()
                .apiKeyId(UUID.randomUUID().toString())
                .build();
        when(apiKeyService.generateApiKeys(any(GenerateApiKeyRequest.class))).thenReturn(apiKeyResp);

        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.fromString(apiKeyResp.getApiKeyId()));
        apiKey.setPublicKey("pub");
        apiKey.setSecretKey("sec");
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setType(ApiKeyType.SANDBOX);
        when(apiKeyRepository.findById(apiKey.getId())).thenReturn(Optional.of(apiKey));

        doNothing().when(ledgerService).initializeLedgerForOrg(any(UUID.class));

        RegisterOrgStaffResponse response = userService.registerOrgUser(request);

        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getBusinessName()).isEqualTo("Acme Inc");
        assertThat(response.getApiPublicKey()).isEqualTo("pub");

        verify(emailService).sendVerificationEmail(eq("john@example.com"), any(), any());
        verify(ledgerService).initializeLedgerForOrg(any(UUID.class));
    }

    @Test
    void loginUser_shouldReturnTokens_whenUserIsActive() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setValue("john@example.com");
        loginRequest.setPassword("pass123");

        Authentication auth = new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), "pass123");
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UUID userId = UUID.fromString(auth.getName());
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setEmailVerificationStatus(OrgStatus.ACTIVE);
        user.setRole(Role.ORG_ADMIN);
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setOrgId("acme123");
        user.setOrganization(org);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any(), any())).thenReturn("refresh");

        LoginResponse response = userService.loginUser(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getMessage()).isEqualTo("Logged in successfully");
    }

    @Test
    void refreshAccessToken_shouldReturnNewAccessToken() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setRole(Role.ORG_ADMIN);
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setOrgId("acme123");
        user.setOrganization(org);

        RefreshToken refresh = new RefreshToken();
        refresh.setToken("refresh123");
        refresh.setUserId(user);
        refresh.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        when(refreshTokenRepository.findByToken("refresh123")).thenReturn(Optional.of(refresh));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("newAccess");

        LoginResponse resp = userService.refreshAccessToken("refresh123");

        assertThat(resp.getAccessToken()).isEqualTo("newAccess");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh123");
    }

    @Test
    void logoutUser_shouldDeleteRefreshTokenAndUpdateUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");

        RefreshToken refresh = new RefreshToken();
        refresh.setToken("refresh123");
        refresh.setUserId(user);
        when(refreshTokenRepository.findByToken("refresh123")).thenReturn(Optional.of(refresh));

        userService.logoutUser("refresh123");

        verify(refreshTokenRepository).delete(refresh);
        verify(userRepository).save(user);
        assertThat(user.isLoggedIn()).isFalse();
    }
}

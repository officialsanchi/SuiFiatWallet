package com.clyrafy.wallet.org.service.impl;


import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.apikey.data.repositories.ApiKeyRepository;
import com.clyrafy.wallet.apikey.dtos.requests.GenerateApiKeyRequest;
import com.clyrafy.wallet.apikey.dtos.response.GenerateApiKeyResponse;
import com.clyrafy.wallet.apikey.enums.ApiKeyStatus;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.apikey.exceptions.ApiKeyNotFoundException;
import com.clyrafy.wallet.apikey.services.ApiKeyService;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.exceptions.UserAlreadyExistsException;
import com.clyrafy.wallet.kyb.enums.KybStatus;
import com.clyrafy.wallet.ledger.service.LedgerService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.models.VerificationToken;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.org.data.repositories.VerificationTokenRepository;
import com.clyrafy.wallet.org.dtos.requests.LoginRequest;
import com.clyrafy.wallet.org.dtos.requests.RegisterOrgStaffRequest;
import com.clyrafy.wallet.org.dtos.responses.GetApiKeyResponse;
import com.clyrafy.wallet.org.dtos.responses.LoginResponse;
import com.clyrafy.wallet.org.dtos.responses.RegisterOrgStaffResponse;
import com.clyrafy.wallet.org.enums.EmailVerificationStatus;
import com.clyrafy.wallet.org.enums.OrgStatus;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.org.exception.OrganizationNotFoundException;
import com.clyrafy.wallet.org.service.EmailService;
import com.clyrafy.wallet.org.service.OrgCodeService;
import com.clyrafy.wallet.org.service.UserService;
import com.clyrafy.wallet.org.service.VerificationTokenService;
import com.clyrafy.wallet.security.JwtUtil;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.dtos.responses.WalletResponse;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.service.WalletService;
import com.clyrafy.wallet.org.data.models.RefreshToken;
import com.clyrafy.wallet.org.data.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final OrganizationRespository organizationRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final ApiKeyService apiKeyService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final OrgCodeService orgCodeService;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EndUserRepository endUserRepository;


    @Value("${org.user.app.verification-url}")
    private String verificationUrl;


    @Value("${org.user.app.redirect-url}")
    private String redirectUrl;


    public UserServiceImpl(UserRepository userRepository,
                           OrganizationRespository organizationRepository,
                           WalletService walletService,
                           LedgerService ledgerService,
                           ApiKeyService apiKeyService,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           RefreshTokenRepository refreshTokenRepository,
                           ApiKeyRepository apiKeyRepository,
                           OrgCodeService orgCodeService,
                           EmailService emailService,
                           VerificationTokenService verificationTokenService,
                           VerificationTokenRepository verificationTokenRepository,
                           EndUserRepository endUserRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.walletService = walletService;
        this.ledgerService = ledgerService;
        this.apiKeyService = apiKeyService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.orgCodeService = orgCodeService;
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.endUserRepository = endUserRepository;
    }



//    @Transactional
//    @Override
//    public RegisterOrgStaffResponse registerOrgUser(@NonNull RegisterOrgStaffRequest request) {
//        validatePasswords(request.getPassword(), request.getConfirmPassword());
//        checkEmailExists(request.getEmail());
//        checkPhoneExists(request.getPhoneNumber());
//
//        String username = generateUniqueUsername(request.getFirstName());
//        Organization organization = initializeOrganizationWithWallet(request, );
//        ledgerService.initializeLedgerForOrg(organization.getId());
//
//        GenerateApiKeyResponse apiKeyResp = generateApiKeyForOrg(organization);
//        ApiKey keyEntity = apiKeyRepository.findById(UUID.fromString(apiKeyResp.getApiKeyId()))
//                .orElseThrow(() -> new IllegalStateException("API key not found"));
//        organization.addApiKey(keyEntity);
//        organizationRepository.saveAndFlush(organization);
//
//        User user = createOrgAdminUser(request, organization);
//        sendVerificationEmail(user);
//
//        return buildRegisterResponse(user, organization);
//    }

    @Transactional
    @Override
    public RegisterOrgStaffResponse registerOrgUser(@NonNull RegisterOrgStaffRequest request) {
        log.info("Starting registration for org user with email [{}]", request.getEmail());

        validatePasswords(request.getPassword(), request.getConfirmPassword());
        checkEmailExists(request.getEmail());
        checkPhoneExists(request.getPhoneNumber());

        String username = generateUniqueUsername(request.getFirstName());
        log.info("Generated unique username [{}]", username);

        User user = getUser(request, username);
        log.info("Org admin user [{}] created with ID [{}]", user.getUserName(), user.getId());

//        Update ths wallettype as it is set manually and initialized
        Organization organization = initializeOrganizationWithWallet(request, user.getId(), WalletType.NGN);
        user.setOrganization(organization);
        userRepository.saveAndFlush(user);

        log.info("Ledger initialized for organization [{}]", organization.getId());
        ledgerService.initializeLedgerForOrg(organization.getId());

        GenerateApiKeyResponse apiKeyResp = generateApiKeyForOrg(organization);
        ApiKey keyEntity = apiKeyRepository.findById(UUID.fromString(apiKeyResp.getApiKeyId()))
                .orElseThrow(() -> new IllegalStateException("API key not found"));

        keyEntity.setOrganization(organization);
        apiKeyRepository.saveAndFlush(keyEntity);
        organization.getApiKeys().add(keyEntity);

        organizationRepository.saveAndFlush(organization);
        sendVerificationEmail(user);
        log.info("Verification email sent to [{}]", user.getEmail());

        return buildRegisterResponse(user, organization);
    }

    private User getUser(RegisterOrgStaffRequest request, String username) {
        User user = new User();
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setUserName(username);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCountry(request.getCountry());
        user.setAcceptTerms(request.isAcceptTerms());
        user.setRole(Role.ORG_ADMIN);
        user.setLoggedIn(false);
        user.setEmailVerificationStatus(OrgStatus.PENDING_VERIFICATION);
        user = userRepository.saveAndFlush(user);
        return user;
    }


    private void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new IllegalArgumentException("Passwords must match");
    }

    private void checkEmailExists(String email) {
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
    }

    private void checkPhoneExists(String phone) {
        if (userRepository.findByPhoneNumber(phone).isPresent()) {
            throw new UserAlreadyExistsException("Phone number already registered");
        }
    }

//    private Organization initializeOrganizationWithWallet(RegisterOrgStaffRequest request, String username) {
//        Organization org = createOrganizationEntity(request);
//        org = organizationRepository.saveAndFlush(org);
//
//        Wallet wallet = walletService.createWalletForOrganization(org.getId(), username);
//        if (wallet == null) {
//            throw new IllegalStateException("Wallet was not created for org " + org.getId());
//        }
//
//        wallet.setOrganization(org);
//        org.getWallets().add(wallet);
//
//        return organizationRepository.saveAndFlush(org);
//    }


    private Organization initializeOrganizationWithWallet(RegisterOrgStaffRequest request, UUID creatorOrgUserId, WalletType walletType) {
        log.info("Initializing organization [{}] with wallet for creator [{}]", request.getBusinessName(), creatorOrgUserId);

        Organization organization = createOrganizationEntity(request);
        organization = organizationRepository.saveAndFlush(organization);
        log.info("Organization [{}] saved with ID [{}]", organization.getName(), organization.getId());

        Wallet wallet = walletService.createWalletForOrganization(organization.getId(), creatorOrgUserId, walletType);
        log.info("Wallet [{}] ready for organization [{}]", wallet.getId(), organization.getId());

        if (organization.getWallets() == null) {
            organization.setWallets(new HashSet<>());
        }
        if (!organization.getWallets().contains(wallet)) {
            wallet.setOrganization(organization);
            organization.getWallets().add(wallet);
            organization = organizationRepository.saveAndFlush(organization);
            log.info("Wallet [{}] linked to organization [{}]", wallet.getId(), organization.getId());
        }

        return organization;
    }


    private Organization createOrganizationEntity(RegisterOrgStaffRequest request) {
        String orgId = generateUniqueOrgId(request);
        log.info("Generating organization entity for [{}], virtual org ID [{}]", request.getBusinessName(), orgId);

        Organization organization = new Organization();
        organization.setName(request.getBusinessName());
        organization.setKybStatus(KybStatus.PENDING);
        organization.setOrgId(orgId);

        organization.setWallets(new HashSet<>());
        return organization;
    }


    private GenerateApiKeyResponse generateApiKeyForOrg(Organization org) {
        ApiKeyType type = org.getKybStatus() == KybStatus.APPROVED ? ApiKeyType.LIVE : ApiKeyType.SANDBOX;
        GenerateApiKeyRequest req = GenerateApiKeyRequest.builder()
                .organizationId(org.getId().toString())
                .environment(type.name())
                .build();
        return apiKeyService.generateApiKeys(req);
    }


    private String generateUniqueOrgId(RegisterOrgStaffRequest request) {
        String code = orgCodeService.generateUniqueOrgCode(request.getBusinessName());
        return request.getBusinessName().toLowerCase().replaceAll("\\s+", "_") + "_" + code;
    }

    private String generateUniqueUsername(String firstName) {
        String base = firstName.toLowerCase();
        String username = base;
        int attempt = 0;
        while (userRepository.existsByUserName(username) && attempt < 10) {
            attempt++;
            username = base + attempt;
        }
        if (attempt == 10) username = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        return username;
    }

    private void sendVerificationEmail(User user) {
        String token = verificationTokenService.generateToken(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName().split(" ")[0], token);
    }


    private RegisterOrgStaffResponse buildRegisterResponse(User user, Organization organization) {
        ApiKey organizationApiKey = organization.getWallets() != null
                ? organization.getApiKeys().stream()
                .filter(apiKey -> apiKey.getStatus() == ApiKeyStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Organization API key not found"))
                : null;


        return RegisterOrgStaffResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .businessName(organization.getName())
                .userId(user.getId().toString())
                .organizationId(String.valueOf(organization.getId()))
                .virtualOrganizationId(organization.getOrgId())
                .KybStatus(organization.getKybStatus().toString())
                .apiPublicKey(organizationApiKey.getPublicKey())
                .apiSecret(organizationApiKey.getSecretKey())
                .apiKeyType(String.valueOf(organizationApiKey.getType()))
                .userName(user.getUserName())
                .country(user.getCountry())
                .message("Account created successfully. Please verify your email.")
                .walletVirtualAccount(
                        String.valueOf(organization.getWallets().stream()
                                .findFirst()
                                .map(wallet -> WalletResponse.builder()
                                        .walletId(wallet.getId().toString())
                                        .virtualAccountNum(wallet.getVirtualAccountNum())
                                        .walletType(wallet.getWalletType().name())
                                        .balance(
                                                wallet.getBalances().stream()
                                                        .findFirst()
                                                        .map(balance -> balance.getBalance())
                                                        .orElse(BigDecimal.ZERO)
                                        )
                                        .build()
                                ).orElse(null))
                )
                .build();
    }


    @Transactional
    @Override
    public String verifyEmailAndLogin(@NonNull String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        User user = verificationToken.getUser();

        if (user.getEmailVerificationStatus() == OrgStatus.ACTIVE) {
            throw new IllegalStateException("Email already verified. Please login manually.");
        }

        user.setEmailVerificationStatus(OrgStatus.ACTIVE);
        user.setLoggedIn(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        TokenPair tokenPair = assignTokensToUser(user);
        emailService.sendEmailVerifiedEmail(user.getEmail(), user.getFullName().split(" ")[0], redirectUrl);

        return UriComponentsBuilder
                .fromUriString(redirectUrl)
                .queryParam("accessToken", tokenPair.accessToken())
                .queryParam("refreshToken", tokenPair.refreshToken())
                .build()
                .toUriString();
    }

    @Override
    public String resendVerificationEmail(@NonNull String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getEmailVerificationStatus() == OrgStatus.ACTIVE)
            throw new IllegalStateException("User is already verified");

        sendVerificationEmail(user);
        return "Verification email resent successfully";
    }

    @Transactional
    @Override
    public EmailVerificationStatus verifyEmail(@NonNull String token) {
        OrgStatus status = verificationTokenService.validateToken(token);
        if (status == OrgStatus.INVALID) return EmailVerificationStatus.INVALID;
        if (status == OrgStatus.EXPIRED) return EmailVerificationStatus.EXPIRED;

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElse(null);
        if (verificationToken == null) return EmailVerificationStatus.INVALID;

        User user = userRepository.findById(verificationToken.getUser().getId())
                .orElse(null);
        if (user == null) return EmailVerificationStatus.USER_NOT_FOUND;

        user.setEmailVerificationStatus(OrgStatus.ACTIVE);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        emailService.sendEmailVerifiedEmail(user.getEmail(), user.getFullName().split(" ")[0], redirectUrl);
        log.info("Email verified for user [{}]", user.getEmail());
        return EmailVerificationStatus.SUCCESS;
    }


    @Override
    public User getUserByEmail(@NonNull String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }


    @Override
    public LoginResponse loginUser(@NonNull LoginRequest loginReq) {
        validateLogin(loginReq);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getValue(), loginReq.getPassword())
        );

        UUID userId = UUID.fromString(auth.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        if (user.getEmailVerificationStatus() != OrgStatus.ACTIVE) {
            throw new IllegalStateException("Email not verified");
        }

        user.setLoggedIn(true);
        TokenPair tokenPair = assignTokensToUser(user);
        userRepository.save(user);

        return buildLoginResponse(tokenPair, user);
    }

    private void validateLogin(LoginRequest req) {
        if (req.getValue() == null || req.getValue().trim().isEmpty() ||
                req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or password cannot be empty");
        }
    }

    private TokenPair assignTokensToUser(User user) {
        String access = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), String.valueOf(user.getRole()));
        String refresh = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken rt = new RefreshToken();
        rt.setToken(refresh);
        rt.setUserId(user);
        rt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiryMs()));
        refreshTokenRepository.save(rt);

        return new TokenPair(access, refresh);
    }

    private LoginResponse buildLoginResponse(TokenPair tokens, User user) {
        return new LoginResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getId().toString(),
                user.getRole().toString(),
                "Logged in successfully",
                user.getOrganization().getOrgId()
        );
    }


    private record TokenPair(String accessToken, String refreshToken) {}


    @Override
    public GetApiKeyResponse getApiKeys(@NonNull ApiKeyType environment) {
        UUID orgId = getCurrentUserOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found"));

        ApiKey apiKey = apiKeyRepository
                .findFirstByOrganizationIdAndTypeAndStatus(org.getId(), environment, ApiKeyStatus.ACTIVE)
                .orElseThrow(() -> new ApiKeyNotFoundException("API key not found"));

        return GetApiKeyResponse.builder()
                .apiKeyId(apiKey.getId().toString())
                .publicKey(apiKey.getPublicKey())
                .secretKey(apiKey.getSecretKey())
                .type(apiKey.getType())
                .orgId(org.getId().toString())
                .build();
    }


    @Override
    public List<GenerateApiKeyResponse> fetchUserApiKeys(@NonNull UUID orgId, @NonNull ApiKeyType type) {
        return apiKeyService.getAllOrganizationApiKeys(orgId, type);
    }


    @Override
    public UUID getCurrentUserOrganizationId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getOrganization().getId();
    }


    @Override
    public void forgotPassword(@NonNull String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with this email"));


        String token = verificationTokenService.generateToken(user);
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName().split(" ")[0],
                redirectUrl + "/reset-password?token=" + token
        );
    }


    @Override
    public void resetPassword(@NonNull String token, @NonNull String newPassword) {
        OrgStatus tokenStatus = verificationTokenService.validateToken(token);
        if (tokenStatus == OrgStatus.INVALID || tokenStatus == OrgStatus.EXPIRED) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }


        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        User user = userRepository.findById(verificationToken.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }


    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired, please login again");
        }

        User user = stored.getUserId();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), String.valueOf(user.getRole()));

        return new LoginResponse(
                newAccessToken,
                refreshToken,
                user.getId().toString(),
                user.getRole().toString(),
                "Token refreshed successfully",
                user.getOrganization().getOrgId()
        );
    }


    @Override
    public void logoutUser(@NonNull String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        refreshTokenRepository.delete(stored);

        User user = stored.getUserId();
        user.setLoggedIn(false);
        userRepository.save(user);

        log.info("User [{}] has been logged out and refresh token revoked", user.getEmail());
    }

    @Override
    public User getUserById(@NonNull UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

}

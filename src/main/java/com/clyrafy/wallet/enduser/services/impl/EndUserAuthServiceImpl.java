package com.clyrafy.wallet.enduser.services.impl;

import com.clyrafy.wallet.commons.exceptions.InvalidLenghtException;
import com.clyrafy.wallet.enduser.dtos.requests.*;
import com.clyrafy.wallet.enduser.dtos.responses.EndUserLoginResponse;
import com.clyrafy.wallet.enduser.dtos.responses.RegisterEndUserResponse;
import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.commons.exceptions.PinMismatchException;
import com.clyrafy.wallet.enduser.services.EndUserAuthService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.org.service.EmailService;
import com.clyrafy.wallet.security.JwtUtil;
import com.clyrafy.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EndUserAuthServiceImpl implements EndUserAuthService {

    @Value("${org.user.app.verification-url}")
    private String endUserVerificationUrl;

    @Value("${end-user.app.redirect-url}")
    private String userAppWalletInterface;

    private final EndUserRepository endUserRepository;
    private final EmailService emailService;
    private final EndUserVerificationTokenServiceImpl verificationTokenService;
    private final OrganizationRespository organizationRespository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final WalletService walletService;

//    @Override
//    public RegisterEndUserResponse registerUser(RegisterEndUserRequest request) {
//        endUserRepository.findByEmail(request.getEmail()).ifPresent(u -> {
//            throw new IllegalStateException("Email already registered");
//        });
//
//        Organization org = organizationRespository.findById(UUID.fromString(request.getOrganizationId()))
//                .orElseThrow(() -> new IllegalStateException("Organization not found"));
//
//        EndUser user = new EndUser();
//        user.setFullName(request.getFullName());
//        user.setEmail(request.getEmail());
//        user.setPhoneNumber(request.getPhoneNumber());
//        user.setOrganization(org);
//
//        onRegisterCheckIfPinsMatch(request, user);
//        user.setPasswordHash(passwordEncoder.encode(request.getPin()));
//        user.setVirtualAccountNumber(generateVirtualAccountNumber(request.getPhoneNumber()));
//        user.setKycVerified(false);
//
//        endUserRepository.save(user);
//
//        CreateWalletForEndUserRequest createWalletForEndUserRequest = CreateWalletForEndUserRequest.builder()
//                .endUser(user)
//                .email(user.getEmail())
//                .userName(user.getFullName())
//                .organization(org)
//                .build();
//        walletService.createWalletsForEndUser(createWalletForEndUserRequest);
//        endUserRepository.save(user);
//
//        String token = verificationTokenService.generateToken(user);
//        emailService.sendVerificationEmail(
//                user.getEmail(),
//                user.getFullName().split(" ")[0],
//                endUserVerificationUrl + token
//        );
//
//        return getRegisterEndUserResponse(user);
//    }

    @Override
    public RegisterEndUserResponse registerUser(RegisterEndUserRequest request) {
        endUserRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email already registered");
        });

        // ✅ Resolve organization (UUID or code)
        Organization org = resolveOrganization(request.getOrganizationId());

        EndUser user = new EndUser();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setOrganization(org);

        onRegisterCheckIfPinsMatch(request, user);
        user.setPasswordHash(passwordEncoder.encode(request.getPin()));
        user.setVirtualAccountNumber(generateVirtualAccountNumber(request.getPhoneNumber()));
        user.setKycVerified(false);

        endUserRepository.save(user);

        CreateWalletForEndUserRequest createWalletForEndUserRequest = CreateWalletForEndUserRequest.builder()
                .endUser(user)
                .email(user.getEmail())
                .userName(user.getFullName())
                .organization(org)
                .build();

        walletService.createWalletsForEndUser(createWalletForEndUserRequest);
        endUserRepository.save(user);

        String token = verificationTokenService.generateToken(user);
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName().split(" ")[0],
                endUserVerificationUrl + token
        );

        return getRegisterEndUserResponse(user);
    }

    /**
     * Resolve an organization by UUID or by code (like SOLUTI9017519196).
     */
    private Organization resolveOrganization(String orgIdentifier) {
        if (isUUID(orgIdentifier)) {
            return organizationRespository.findById(UUID.fromString(orgIdentifier))
                    .orElseThrow(() -> new IllegalStateException("Organization not found by UUID: " + orgIdentifier));
        } else {
            return organizationRespository.findByOrgId(orgIdentifier)
                    .orElseThrow(() -> new IllegalStateException("Organization not found by code: " + orgIdentifier));
        }
    }

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void onRegisterCheckIfPinsMatch(RegisterEndUserRequest request, EndUser user) {
        if (request.getPin().equals(request.getConfirmPin())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPin()));
        } else throw new PinMismatchException("The entered pin does not match");
    }

    private static RegisterEndUserResponse getRegisterEndUserResponse(EndUser user) {
        return new RegisterEndUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getVirtualAccountNumber(),
                "Account created successfully. Please verify your email."
        );
    }

    @Override
    public boolean verifyEmail(String token) {
        EndUser user = verificationTokenService.validateToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        user.setKycVerified(true);
        endUserRepository.save(user);

        emailService.sendEmailVerifiedEmail(
                user.getEmail(),
                user.getFullName().split(" ")[0],
                userAppWalletInterface
        );

        return true;
    }

    @Override
    public String resendVerificationEmail(String email) {
        EndUser user = endUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("End user not found"));

        if (user.isKycVerified()) throw new IllegalStateException("User already verified");

        String token = verificationTokenService.generateToken(user);
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName().split(" ")[0],
                endUserVerificationUrl + token
        );

        return "Verification email resent successfully. Please check your inbox.";
    }

    @Override
    public EndUserLoginResponse loginUser(LoginEndUserRequest request) {
        EndUser user = endUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("End user not found"));

        checkLoginDetails(request);
        doesPinLengthEqual4AndDoesItMatch(request, user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), Role.END_USER.toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        return new EndUserLoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getFullName(),
                user.getOrganization().getId(),
                "Logged in successfully"
        );
    }

    private void checkLoginDetails(LoginEndUserRequest req) {
        if (req.getEmail() == null || req.getPin().trim().isEmpty() || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or password cannot be empty");
        }
    }

    private void doesPinLengthEqual4AndDoesItMatch(LoginEndUserRequest request, EndUser user) {
        if (request.getPin().length() != 4)
            throw new InvalidLenghtException("Enter 4 digits");

        if (!passwordEncoder.matches(request.getPin(), user.getPasswordHash()))
            throw new PinMismatchException("Invalid username or password");
    }


    @Override
    public void forgotPassword(String email) {
        EndUser user = endUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = verificationTokenService.generateToken(user);
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName().split(" ")[0],
                userAppWalletInterface + token
        );
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        EndUser user = verificationTokenService.validateToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        endUserRepository.save(user);
    }

    private String generateVirtualAccountNumber(String phoneNumber) {
        String accountNumber = phoneNumber.substring(Math.max(phoneNumber.length() - 10, 0));

        boolean existsInEndUser = endUserRepository.existsByVirtualAccountNumber(accountNumber);


        return accountNumber;
    }



}

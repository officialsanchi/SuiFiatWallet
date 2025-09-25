package com.clyrafy.wallet.org.controllers;

import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.dtos.requests.*;
import com.clyrafy.wallet.org.dtos.responses.LoginResponse;
import com.clyrafy.wallet.org.dtos.responses.RegisterOrgStaffResponse;
import com.clyrafy.wallet.org.enums.EmailVerificationStatus;
import com.clyrafy.wallet.org.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class
AuthController {

    @Value("${frontendBaseUrl}")
    private String frontendBaseUrl;

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterOrgStaffRequest request) {
        RegisterOrgStaffResponse response = userService.registerOrgUser(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + response.getUserId()))
                .body(Map.of(
                        "message", "User registered successfully",
                        "data", response
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Refresh token is required",
                    "status", "failed"
            ));
        }
        try {
            userService.logoutUser(refreshToken);
            return ResponseEntity.ok(Map.of(
                    "message", "User logged out successfully",
                    "status", "success"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "failed"
            ));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        EmailVerificationStatus status = userService.verifyEmail(request.getToken());
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully",
                    "status", "verified"
            ));
            case INVALID, EXPIRED, USER_NOT_FOUND -> ResponseEntity.badRequest().body(Map.of(
                    "message", status.name(),
                    "status", "failed"
            ));
            case VALID -> ResponseEntity.badRequest().body(Map.of(
                    "message", "Unexpected status",
                    "status", "failed"
            ));
        };
    }

    @GetMapping("/verify-email-login")
    public void verifyEmailAndLoginRedirect(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        try {
            String redirectWithTokens = userService.verifyEmailAndLogin(token);

            response.sendRedirect(redirectWithTokens);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.sendRedirect(frontendBaseUrl + "/auth/login?error=" + e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(userService.resendVerificationEmail(request.getEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Password reset email sent successfully. Please check your inbox.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful. You can now login with your new password.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.refreshAccessToken(body.get("refreshToken")));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }

        UUID userId = UUID.fromString(authentication.getName());
        User user = userService.getUserById(userId);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId().toString(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole().toString(),
                "organizationId", user.getOrganization().getOrgId(),
                "businessName", user.getOrganization().getName()
        ));
    }

}

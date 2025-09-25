package com.clyrafy.wallet.enduser.controller;

import com.clyrafy.wallet.enduser.dtos.requests.LoginEndUserRequest;
import com.clyrafy.wallet.enduser.dtos.requests.RegisterEndUserRequest;
import com.clyrafy.wallet.enduser.dtos.responses.EndUserLoginResponse;
import com.clyrafy.wallet.enduser.dtos.responses.RegisterEndUserResponse;
import com.clyrafy.wallet.enduser.services.EndUserAuthService;
import com.clyrafy.wallet.org.dtos.requests.ForgotPasswordRequest;
import com.clyrafy.wallet.org.dtos.requests.ResendVerificationRequest;
import com.clyrafy.wallet.org.dtos.requests.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/endusers")
@RequiredArgsConstructor
public class EndUserController {

    private final EndUserAuthService endUserAuthService;

    @PostMapping("/register")
    public ResponseEntity<RegisterEndUserResponse> register(@RequestBody @Valid RegisterEndUserRequest request) {
        return ResponseEntity.ok(endUserAuthService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<EndUserLoginResponse> login(@RequestBody @Valid LoginEndUserRequest request) {
        return ResponseEntity.ok(endUserAuthService.loginUser(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = endUserAuthService.verifyEmail(token);
        return ResponseEntity.ok(verified ? "Email verified successfully" : "Email verification failed");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(endUserAuthService.resendVerificationEmail(request.getEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        endUserAuthService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        endUserAuthService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful");
    }
}

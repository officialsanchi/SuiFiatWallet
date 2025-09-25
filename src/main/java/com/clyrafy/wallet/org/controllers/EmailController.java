package com.clyrafy.wallet.org.controllers;

import com.clyrafy.wallet.org.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/verification")
    public ResponseEntity<?> sendVerificationEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String verificationUrl
    ) {
        emailService.sendVerificationEmail(to, firstName, verificationUrl);
        return ResponseEntity.ok(Map.of("message", "Verification email sent to " + to));
    }

    @PostMapping("/verified")
    public ResponseEntity<?> sendEmailVerifiedEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String dashboardUrl
    ) {
        emailService.sendEmailVerifiedEmail(to, firstName, dashboardUrl);
        return ResponseEntity.ok(Map.of("message", "Welcome email sent to " + to));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> sendPasswordResetEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String resetUrl
    ) {
        emailService.sendPasswordResetEmail(to, firstName, resetUrl);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent to " + to));
    }

    @PostMapping("/new-login")
    public ResponseEntity<?> sendNewLoginAlertEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String secureAccountUrl
    ) {
        emailService.sendNewLoginAlertEmail(to, firstName, secureAccountUrl);
        return ResponseEntity.ok(Map.of("message", "New login alert sent to " + to));
    }

    @PostMapping("/api-key")
    public ResponseEntity<?> sendApiKeyCreatedEmail(
            @RequestParam String to,
            @RequestParam String firstName,
            @RequestParam String keysUrl
    ) {
        emailService.sendApiKeyCreatedEmail(to, firstName, keysUrl);
        return ResponseEntity.ok(Map.of("message", "API key creation email sent to " + to));
    }
}

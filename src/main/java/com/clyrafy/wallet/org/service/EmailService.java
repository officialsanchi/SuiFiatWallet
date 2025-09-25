package com.clyrafy.wallet.org.service;

public interface EmailService {
    void sendVerificationEmail(String to, String firstName, String verificationUrl);
    void sendEmailVerifiedEmail(String to, String firstName, String dashboardUrl);
    void sendPasswordResetEmail(String to, String firstName, String resetUrl);
    void sendNewLoginAlertEmail(String to, String firstName, String secureAccountUrl);
    void sendApiKeyCreatedEmail(String to, String firstName, String keysUrl);
}

package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.org.service.EmailService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTestService {
    private final EmailService emailService;

    @PostConstruct
    public void testEmailService() {
        try {
            log.info("Testing email service on application startup");
//            emailService.sendVerificationEmail(
//                    "johdanike@gmail.com",
//                    "TestUser",
//                    "http://localhost:1946/api/users/verify-email?token=test-token"
//            );
            log.info("Test email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send test email: {}", e.getMessage(), e);
        }
    }
}
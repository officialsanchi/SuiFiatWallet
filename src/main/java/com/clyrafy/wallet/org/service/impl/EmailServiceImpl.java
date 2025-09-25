package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.apikey.dtos.response.WebhookService;
import com.clyrafy.wallet.org.exception.EmailSendException;
import com.clyrafy.wallet.org.service.EmailService;
import com.clyrafy.wallet.template.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${verification.base.url}")
    private String verificationBaseUrl;

    private final JavaMailSender mailSender;
    private final WebhookService webhookService;

    private void sendEmail(
            @NonNull String to,
            @NonNull String subject,
            @NonNull String htmlBody,
            @NonNull String textBody,
            @NonNull String eventType
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            mailSender.send(mimeMessage);

            try {
                webhookService.emitWebhook("email.sent", to, subject);
            } catch (Exception webhookEx) {
                log.warn("Webhook failed after sending {} email to {}: {}", eventType, to, webhookEx.getMessage());
            }

            log.info("Sent {} email to {}", eventType, to);

        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send {} email to {}: {}", eventType, to, e.getMessage());
            throw new EmailSendException("Email sending failed for " + eventType, e);
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(@NonNull String to, @NonNull String firstName, @NonNull String token) {
        String backendVerifyUrl = String.format("%s/api/users/verify-email-login?token=%s",
                verificationBaseUrl, token);

        sendEmail(
                to,
                "Verify your ClyraFi email",
                EmailTemplate.verifyEmailHtml(firstName, backendVerifyUrl),
                EmailTemplate.verifyEmailText(firstName, backendVerifyUrl),
                "verification"
        );
    }

    @Async
    @Override
    public void sendEmailVerifiedEmail(@NonNull String to, @NonNull String firstName, @NonNull String dashboardUrl) {
        sendEmail(
                to,
                "Welcome — sandbox ready",
                EmailTemplate.emailVerifiedHtml(firstName, dashboardUrl),
                EmailTemplate.emailVerifiedText(firstName, dashboardUrl),
                "email_verified"
        );
    }

    @Async
    @Override
    public void sendPasswordResetEmail(@NonNull String to, @NonNull String firstName, @NonNull String resetUrl) {
        sendEmail(
                to,
                "Reset your password",
                EmailTemplate.passwordResetHtml(firstName, resetUrl),
                EmailTemplate.passwordResetText(firstName, resetUrl),
                "password_reset"
        );
    }

    @Async
    @Override
    public void sendNewLoginAlertEmail(@NonNull String to, @NonNull String firstName, @NonNull String secureAccountUrl) {
        sendEmail(
                to,
                "New sign-in alert",
                EmailTemplate.newLoginHtml(firstName, secureAccountUrl),
                EmailTemplate.newLoginText(firstName, secureAccountUrl),
                "new_login"
        );
    }

    @Async
    @Override
    public void sendApiKeyCreatedEmail(@NonNull String to, @NonNull String firstName, @NonNull String keysUrl) {
        sendEmail(
                to,
                "Sandbox keys ready",
                EmailTemplate.apiKeyCreatedHtml(firstName, keysUrl),
                EmailTemplate.apiKeyCreatedText(firstName, keysUrl),
                "api_key_created"
        );
    }
}

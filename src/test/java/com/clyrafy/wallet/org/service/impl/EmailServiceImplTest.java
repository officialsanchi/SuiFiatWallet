package com.clyrafy.wallet.org.service.impl;

import com.clyrafy.wallet.apikey.dtos.response.WebhookService;
import com.clyrafy.wallet.org.exception.EmailSendException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private WebhookService webhookService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void sendVerificationEmail_shouldSendAndEmitWebhook() {
        emailService.sendVerificationEmail("john@example.com", "John", "http://verify.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }

    @Test
    public void sendEmailVerifiedEmail_shouldSendAndEmitWebhook() {
        emailService.sendEmailVerifiedEmail("john@example.com", "John", "http://dashboard.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }

    @Test
    public void sendPasswordResetEmail_shouldSendAndEmitWebhook() {
        emailService.sendPasswordResetEmail("john@example.com", "John", "http://reset.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }

    @Test
    public void sendNewLoginAlertEmail_shouldSendAndEmitWebhook() {
        emailService.sendNewLoginAlertEmail("john@example.com", "John", "http://secure.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }

    @Test
    public void sendApiKeyCreatedEmail_shouldSendAndEmitWebhook() {
        emailService.sendApiKeyCreatedEmail("john@example.com", "John", "http://keys.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }


    @Test
    void sendEmail_shouldThrowEmailSendExceptionWhenSendFails() {
        doThrow(new MailSendException("send fail"))
                .when(mailSender)
                .send(any(MimeMessage.class));

        assertThrows(EmailSendException.class, () ->
                emailService.sendVerificationEmail("john@example.com", "John", "token123")
        );
    }

    @Test
    public void sendEmail_shouldContinueWhenWebhookFails() {
        doThrow(new RuntimeException("webhook fail"))
                .when(webhookService).emitWebhook(anyString(), anyString(), anyString());

        emailService.sendVerificationEmail("john@example.com", "John", "http://verify.com");

        verify(mailSender).send(mimeMessage);
        verify(webhookService).emitWebhook(eq("email.sent"), eq("john@example.com"), anyString());
    }
}

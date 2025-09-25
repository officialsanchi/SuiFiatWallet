package com.clyrafy.wallet.org.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
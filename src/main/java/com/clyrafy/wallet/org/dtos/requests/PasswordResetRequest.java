package com.clyrafy.wallet.org.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @Email
    @NotBlank
    private String email;
}

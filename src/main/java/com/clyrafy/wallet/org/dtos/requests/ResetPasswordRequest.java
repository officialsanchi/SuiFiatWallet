package com.clyrafy.wallet.org.dtos.requests;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}
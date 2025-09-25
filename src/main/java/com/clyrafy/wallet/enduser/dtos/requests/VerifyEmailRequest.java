package com.clyrafy.wallet.enduser.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyEmailRequest {
    @NotBlank
    private String token;
}

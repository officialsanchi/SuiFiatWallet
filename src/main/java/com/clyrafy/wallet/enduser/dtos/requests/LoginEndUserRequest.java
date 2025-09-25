package com.clyrafy.wallet.enduser.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginEndUserRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 4, min = 1)
    private String pin;
}

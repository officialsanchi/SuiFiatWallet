package com.clyrafy.wallet.enduser.dtos.requests;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class RegisterEndUserRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @NotBlank
    @Size(max = 4)
    private String pin;

    private String confirmPin;

    @NonNull
    private String organizationId;
}
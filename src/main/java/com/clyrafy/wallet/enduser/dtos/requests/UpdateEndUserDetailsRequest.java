package com.clyrafy.wallet.enduser.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateEndUserDetailsRequest {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    private String pin;

    @Size(min = 4, max = 4, message = "Confirm PIN must be exactly 4 digits")
    private String confirmPin;
}

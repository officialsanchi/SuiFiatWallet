package com.clyrafy.wallet.enduser.dtos.responses;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterEndUserResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String virtualAccountNumber;
    private String message;
}

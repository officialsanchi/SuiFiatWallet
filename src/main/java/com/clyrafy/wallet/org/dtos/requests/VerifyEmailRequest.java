package com.clyrafy.wallet.org.dtos.requests;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailRequest {
    private String token;
}

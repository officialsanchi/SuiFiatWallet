package com.clyrafy.wallet.org.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyEmailResponse {
    private String message;
    private String dashboardUrl;
}

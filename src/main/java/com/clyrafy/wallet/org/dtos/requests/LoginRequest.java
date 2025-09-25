package com.clyrafy.wallet.org.dtos.requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String value;
    private String password;
}

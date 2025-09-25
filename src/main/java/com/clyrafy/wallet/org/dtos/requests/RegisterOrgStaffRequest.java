package com.clyrafy.wallet.org.dtos.requests;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterOrgStaffRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
    private String businessName;
    private String country;
    private boolean acceptTerms;
}

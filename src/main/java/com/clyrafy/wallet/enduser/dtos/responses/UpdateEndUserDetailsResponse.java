package com.clyrafy.wallet.enduser.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateEndUserDetailsResponse {
    private String endUserId;
    private String fullName;
    private String email;
    private String message;
}

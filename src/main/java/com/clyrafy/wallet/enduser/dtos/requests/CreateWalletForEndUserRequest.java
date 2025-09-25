package com.clyrafy.wallet.enduser.dtos.requests;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.org.data.models.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class CreateWalletForEndUserRequest {
    private EndUser endUser;
    private String email;
    private String userName;
    private Organization organization;

}

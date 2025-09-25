package com.clyrafy.wallet.enduser.services;

import com.clyrafy.wallet.enduser.dtos.requests.*;
import com.clyrafy.wallet.enduser.dtos.responses.EndUserLoginResponse;
import com.clyrafy.wallet.enduser.dtos.responses.RegisterEndUserResponse;

public interface EndUserAuthService {

    RegisterEndUserResponse registerUser(RegisterEndUserRequest request);

    boolean verifyEmail(String token);

    String resendVerificationEmail(String email);

    EndUserLoginResponse loginUser(LoginEndUserRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}

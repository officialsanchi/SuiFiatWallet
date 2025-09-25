package com.clyrafy.wallet.org.service;


import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.enums.OrgStatus;

import java.util.Optional;

public interface VerificationTokenService {
    String generateToken(User user);

    String generateEndUserToken(EndUser user);

    OrgStatus validateToken(String token);

    Optional<OrgStatus> validateEndUserToken(String token);
}

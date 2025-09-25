package com.clyrafy.wallet.wallet.service;

import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;

import java.util.List;
import java.util.UUID;

public interface OrgUserWalletAccessService {

    OrgUserWalletAccess grantAccess(UUID orgUserId, UUID walletId, boolean isAdmin);

    List<OrgUserWalletAccess> getUserWallets(UUID orgUserId);

    void revokeAccess(UUID orgUserId, UUID walletId);
}

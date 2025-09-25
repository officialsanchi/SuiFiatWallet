package com.clyrafy.wallet.wallet.service;

import com.clyrafy.wallet.enduser.dtos.requests.CreateWalletForEndUserRequest;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.enums.WalletOwnerType;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletService {

//    @Transactional
//    Wallet createWalletForOrganization(UUID organizationId, String username);

    @Transactional
    Wallet createWalletForOrganization(UUID organizationId, UUID creatorUserId, WalletType walletType);

    @Transactional
    Wallet createWalletsForEndUser(CreateWalletForEndUserRequest request);


    @Transactional
    OrgUserWalletAccess grantOrgUserAccess(UUID walletId, UUID orgUserId, boolean isAdmin);

    @Transactional
    void revokeOrgUserAccess(UUID walletId, UUID orgUserId);

    List<Wallet> getOrganizationWalletsForOrganizationStaff(UUID orgUserId);

    @Transactional
    WalletBalance updateWalletBalance(UUID walletId, WalletType currency, BigDecimal amount);

    BigDecimal getBalance(UUID walletId, WalletType walletType);

    Optional<Wallet> getWalletById(UUID walletId);

    Optional<Wallet> getWalletByOwner(UUID ownerId, WalletOwnerType ownerType);

    List<Wallet> getWalletByOrganizationId(UUID organizationId);
}

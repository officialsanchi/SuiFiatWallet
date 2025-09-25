package com.clyrafy.wallet.wallet.service.impl;

import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletId;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.repositories.OrgUserWalletAccessRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import com.clyrafy.wallet.wallet.service.OrgUserWalletAccessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrgUserWalletAccessServiceImpl implements OrgUserWalletAccessService {

    private final OrgUserWalletAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Override
    public OrgUserWalletAccess grantAccess(UUID orgUserId, UUID walletId, boolean isAdmin) {
        User user = userRepository.findById(orgUserId)
                .orElseThrow(() -> new IllegalArgumentException("Org user not found"));
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        OrgUserWalletAccess access = new OrgUserWalletAccess();
        access.setId(new OrgUserWalletId(orgUserId, walletId));
        access.setUser(user);
        access.setWallet(wallet);
        access.setAdmin(isAdmin);

        return accessRepository.save(access);
    }

    @Override
    public List<OrgUserWalletAccess> getUserWallets(UUID orgUserId) {
        return accessRepository.findByUserId(orgUserId);
    }

    @Override
    public void revokeAccess(UUID orgUserId, UUID walletId) {
        OrgUserWalletId id = new OrgUserWalletId(orgUserId, walletId);
        accessRepository.deleteById(id);
    }
}

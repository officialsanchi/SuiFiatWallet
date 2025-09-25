package com.clyrafy.wallet.wallet.data.repositories;

import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletId;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgUserWalletAccessRepository extends JpaRepository<OrgUserWalletAccess, OrgUserWalletId> {

    List<OrgUserWalletAccess> findByUserId(UUID userId);
    Optional<OrgUserWalletAccess> findFirstByUserId(UUID userId);

    List<OrgUserWalletAccess> findByWalletId(UUID walletId);

    Optional<OrgUserWalletAccess> findByUserIdAndWalletId(UUID userId, UUID walletId);

    @Query("SELECT o.wallet FROM OrgUserWalletAccess o " +
            "JOIN WalletBalance wb ON wb.wallet.id = o.wallet.id " +
            "WHERE o.user.id = :userId " +
            "AND wb.currencyType = :walletType")
    List<Wallet> findWalletsByUserIdAndWalletType(@Param("userId") UUID userId,
                                                  @Param("walletType") WalletType walletType);

    Optional<OrgUserWalletAccess> findFirstByWalletId(UUID walletId);
    List<OrgUserWalletAccess> findByWallet_Id(UUID walletId);

}

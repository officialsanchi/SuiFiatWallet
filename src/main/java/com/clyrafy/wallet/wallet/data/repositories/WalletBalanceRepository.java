package com.clyrafy.wallet.wallet.data.repositories;

import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, UUID> {

    Optional<WalletBalance> findByWalletIdAndCurrencyType(UUID walletId, WalletType type);

    List<WalletBalance> findByWalletId(UUID walletId);

    Optional<WalletBalance> findByWalletEndUserIdAndCurrencyType(UUID endUserId, WalletType currencyType);

    Collection<WalletBalance> findAllByWalletId(UUID walletId);

    @Query("SELECT wb.wallet FROM WalletBalance wb " +
            "WHERE wb.wallet.endUser.id = :endUserId AND wb.currencyType = :currencyType")
    Optional<Wallet> findWalletByEndUserIdAndCurrency(@Param("endUserId") UUID endUserId,
                                                      @Param("currencyType") WalletType type);

    @Query("SELECT wb.wallet FROM WalletBalance wb " +
            "WHERE wb.wallet.organization.id = :orgId AND wb.currencyType = :currencyType")
    Optional<Wallet> findWalletByOrganizationIdAndCurrency(@Param("orgId") UUID orgId,
                                                           @Param("currencyType") WalletType type);

    @Query("SELECT wb.wallet FROM WalletBalance wb " +
            "WHERE wb.wallet.virtualAccountNum = :virtualAccountNum AND wb.currencyType = :currencyType")
    Collection<Wallet> findWalletsByVirtualAccountNumAndCurrency(@Param("virtualAccountNum") String virtualAccountNum,
                                                                 @Param("currencyType") WalletType type);

    // ✅ FIXED: Return Optional<WalletBalance>
    Optional<WalletBalance> findByWalletAndCurrencyType(Wallet wallet, WalletType type);
}

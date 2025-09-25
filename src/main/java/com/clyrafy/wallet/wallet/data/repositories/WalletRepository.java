package com.clyrafy.wallet.wallet.data.repositories;

import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByVirtualAccountNum(String virtualAccountNum);

    Optional<Wallet> findByEndUserId(UUID endUserId);

    Optional<Wallet> findByOrganizationId(UUID organizationId);

    List<Wallet> findAllByEndUserId(UUID endUserId);

    List<Wallet> findAllByOrganizationId(UUID organizationId);

    @Query("SELECT o.wallet FROM OrgUserWalletAccess o WHERE o.user.id = :userId")
    Optional<Wallet> findByUserId(@Param("userId") UUID userId);

    Optional<Wallet> findByEndUserIdAndWalletType(UUID id, WalletType walletType);

    List<Wallet> findAllByEndUserIdAndWalletType(UUID id, WalletType type);

    boolean existsByVirtualAccountNum(String candidate);

    Optional<Wallet> findFirstByOrganizationIdAndWalletType(UUID id, WalletType type);

    @Query("SELECT w FROM Wallet w JOIN w.organization o JOIN o.staff s WHERE s.email = :email AND w.walletType = :type")
    Optional<Wallet> findByOrganizationEmailAndType(@Param("email") String email, @Param("type") WalletType type);

    @Query("SELECT w FROM Wallet w JOIN w.organization o JOIN o.staff s WHERE s.userName = :username AND w.walletType = :type")
    Optional<Wallet> findByOrganizationUsernameAndType(@Param("username") String username, @Param("type") WalletType type);

}


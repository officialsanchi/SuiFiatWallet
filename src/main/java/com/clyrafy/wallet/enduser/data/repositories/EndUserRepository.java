package com.clyrafy.wallet.enduser.data.repositories;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EndUserRepository extends JpaRepository<EndUser, UUID> {

    Optional<EndUser> findByEmail(String email);

    boolean existsByVirtualAccountNumber(String accountNumber);

    Optional<EndUser> findByUsername(String username);

    @Query("SELECT e FROM EndUser e JOIN e.wallets w WHERE w.id = :walletId")
    Optional<EndUser> findByWalletId(@Param("walletId") UUID walletId);

}

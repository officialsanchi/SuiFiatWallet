package com.clyrafy.wallet.org.data.repositories;

import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);

    User findByUserName(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUserNameIgnoreCase(String userName);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailIgnoreCaseOrUserNameIgnoreCaseOrPhoneNumber(String email, String userName, String phoneNumber);

    boolean existsByUserName(String username);

    @Query("""
    SELECT u FROM User u
    JOIN OrgUserWalletAccess a ON a.user = u
    WHERE a.wallet.virtualAccountNum = :walletNumber
""")
    Optional<User> findByWalletVirtualAccountNum(@Param("walletNumber") String walletNumber);

    Optional<User> findFirstByOrganizationIdAndRole(UUID organizationId, Role attr0);
}
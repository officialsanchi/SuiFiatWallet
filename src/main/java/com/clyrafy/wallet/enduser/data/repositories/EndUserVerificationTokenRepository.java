package com.clyrafy.wallet.enduser.data.repositories;

import com.clyrafy.wallet.enduser.data.models.EndUserVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EndUserVerificationTokenRepository extends JpaRepository<EndUserVerificationToken, UUID> {
    Optional<EndUserVerificationToken> findByToken(String token);
    void deleteAllByEndUserId(UUID endUserId);
}

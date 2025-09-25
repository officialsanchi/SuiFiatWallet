package com.clyrafy.wallet.org.data.repositories;

import com.clyrafy.wallet.org.data.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenAndUserEmail(String token, String email);
    Optional<VerificationToken> findByUserId(UUID userId);
    Optional<VerificationToken> findByToken(String token);
    void deleteAllByUserId(UUID userId);
}
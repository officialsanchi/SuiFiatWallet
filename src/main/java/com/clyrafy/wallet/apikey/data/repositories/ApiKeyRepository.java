package com.clyrafy.wallet.apikey.data.repositories;

import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.apikey.enums.ApiKeyStatus;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByOrganizationIdAndTypeAndStatus(UUID organizationId, ApiKeyType type, ApiKeyStatus status);

    long countByOrganizationIdAndTypeAndStatus(UUID id, ApiKeyType apiKeyType, ApiKeyStatus apiKeyStatus);

    Collection<ApiKey> findByOrganizationId(UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE ApiKey a SET a.status = 'INACTIVE', a.expiredAt = :now " +
            "WHERE a.organization.id = :orgId AND a.type = :type AND a.status = 'ACTIVE'")
    void inactivateKeys(UUID orgId, ApiKeyType type, LocalDateTime now);

    Optional<ApiKey> findFirstByOrganizationIdAndTypeAndStatus(UUID orgId, ApiKeyType environment, ApiKeyStatus apiKeyStatus);
}

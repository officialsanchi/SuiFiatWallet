package com.clyrafy.wallet.org.data.repositories;

import com.clyrafy.wallet.org.data.models.OrgUniqueCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrgUniqueCodeRepository extends JpaRepository<OrgUniqueCode, Long> {
    boolean existsByCodeAndOrgId(String code, String orgId);

    Optional<OrgUniqueCode> findByCodeAndOrgId(String code, String orgId);

    @Query("SELECT uc FROM OrgUniqueCode uc WHERE uc.orgId = :orgId AND uc.status = 'AVAILABLE' ORDER BY uc.createdAt ASC LIMIT 1")
    Optional<OrgUniqueCode> findNextAvailableCode(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE OrgUniqueCode uc SET uc.status = 'USED', uc.usedAt = CURRENT_TIMESTAMP WHERE uc.code = :code AND uc.orgId = :orgId AND uc.status = 'AVAILABLE'")
    int markCodeAsUsed(@Param("code") String code, @Param("orgId") String orgId);
}

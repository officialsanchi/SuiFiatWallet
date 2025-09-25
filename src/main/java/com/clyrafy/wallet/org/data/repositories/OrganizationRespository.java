package com.clyrafy.wallet.org.data.repositories;

import com.clyrafy.wallet.org.data.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRespository extends JpaRepository<Organization, UUID> {
    Organization findOrganizationById(UUID id);

    Optional<Organization> findByOrgId(String organizationId);
}

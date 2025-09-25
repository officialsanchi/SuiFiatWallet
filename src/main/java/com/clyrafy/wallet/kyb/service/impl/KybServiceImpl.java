package com.clyrafy.wallet.kyb.service.impl;

import com.clyrafy.wallet.kyb.enums.KybStatus;
import com.clyrafy.wallet.kyb.service.KybService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.org.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KybServiceImpl implements KybService {
    private final OrganizationRespository organizationRepository;
    private final UserService userService;

    public KybServiceImpl(OrganizationRespository organizationRepository, UserService userService) {
        this.organizationRepository = organizationRepository;
        this.userService = userService;
    }

    @Override
    public KybStatus getKybStatus(UUID orgId) {
        return organizationRepository.findById(orgId)
                .map(Organization::getKybStatus)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
    }

    @Override
    public void updateKycStatus(UUID orgId, KybStatus status) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        if (org.getKybStatus() == KybStatus.APPROVED && status == KybStatus.PENDING) {
            throw new IllegalStateException("Cannot downgrade an APPROVED organization back to PENDING_VERIFICATION");
        }
        org.setKybStatus(status);
        organizationRepository.save(org);
    }

}

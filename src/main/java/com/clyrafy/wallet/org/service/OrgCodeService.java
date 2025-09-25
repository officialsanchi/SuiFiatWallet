package com.clyrafy.wallet.org.service;

import org.springframework.stereotype.Component;

@Component
public interface OrgCodeService {
    String generateUniqueOrgCode(String OrgId);
}

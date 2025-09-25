package com.clyrafy.wallet.kyb.service;


import com.clyrafy.wallet.kyb.enums.KybStatus;

import java.util.UUID;

public interface KybService {
    KybStatus getKybStatus(UUID orgId);
    void updateKycStatus(UUID orgId, KybStatus status);
}

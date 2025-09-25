package com.clyrafy.wallet.ledger.service;

import com.clyrafy.wallet.ledger.dtos.requests.LedgerEntryDto;

import java.util.UUID;

public interface LedgerService {
    void initializeLedgerForOrg(UUID OrgId);
    void recordWithdrawal(LedgerEntryDto ledgerEntryDto);
}

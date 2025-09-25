package com.clyrafy.wallet.ledger.service.impl;

import com.clyrafy.wallet.ledger.data.models.Ledger;
import com.clyrafy.wallet.ledger.data.repositories.LedgerRepository;
import com.clyrafy.wallet.ledger.dtos.requests.LedgerEntryDto;
import com.clyrafy.wallet.ledger.service.LedgerService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;
    private final OrganizationRespository organizationRespository;

    public LedgerServiceImpl(LedgerRepository ledgerRepository, OrganizationRespository organizationRespository) {
        this.ledgerRepository = ledgerRepository;
        this.organizationRespository = organizationRespository;
    }

    @Override
    public void initializeLedgerForOrg(UUID orgId) {
        for (WalletType walletType : WalletType.values()) {
            Ledger ledger = new Ledger();
            ledger.setOrganizationId(orgId);
            ledger.setAmount(BigDecimal.ZERO);
            ledger.setWalletType(walletType);
            ledger.setTransactionType("INITIALIZATION");
            ledger.setInitiatorType("SYSTEM");

            ledger.setCreatedAt(LocalDateTime.now());
            ledger.setUpdatedAt(LocalDateTime.now());

            ledgerRepository.save(ledger);
            log.info("Initialized {} ledger for org: {}", walletType, orgId);
        }
    }


    @Override
    public void recordWithdrawal(LedgerEntryDto ledgerEntryDto) {
        Organization org = organizationRespository.findById(ledgerEntryDto.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found for walletId"));

        Ledger ledger = new Ledger();
        ledger.setOrganizationId(org.getId());
        ledger.setAmount(ledgerEntryDto.getAmount().negate());
        ledger.setWalletType(ledgerEntryDto.getWalletType());
        ledger.setTransactionType("WITHDRAWAL");
        ledger.setInitiatorType("USER");

        ledger.setCreatedAt(LocalDateTime.now());
        ledger.setUpdatedAt(LocalDateTime.now());

        ledgerRepository.save(ledger);
        log.info("Withdrawal of {} {} recorded for org {}",
                ledger.getAmount(), ledger.getWalletType(), org.getId());
    }
}
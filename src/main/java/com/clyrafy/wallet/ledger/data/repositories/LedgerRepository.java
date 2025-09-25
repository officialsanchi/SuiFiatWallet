package com.clyrafy.wallet.ledger.data.repositories;

import com.clyrafy.wallet.ledger.data.models.Ledger;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerRepository extends JpaRepository<Wallet, UUID> {
    void save(Ledger ledger);
}

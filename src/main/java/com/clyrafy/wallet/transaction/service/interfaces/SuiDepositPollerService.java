package com.clyrafy.wallet.transaction.service.interfaces;

import org.springframework.scheduling.annotation.Scheduled;

public interface SuiDepositPollerService {
    @Scheduled(fixedRate = 60000)
    void pollSuiDeposits();
}

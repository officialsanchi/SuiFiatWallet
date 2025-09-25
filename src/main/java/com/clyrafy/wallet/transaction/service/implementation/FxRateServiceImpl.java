package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.transaction.service.interfaces.FxRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FxRateServiceImpl implements FxRateService {

    private final RestTemplate restTemplate;

    private static final String COINGECKO_URL =
            "https://api.coingecko.com/api/v3/simple/price?ids=sui&vs_currencies=ngn";

    private static final String EXCHANGE_RATE_HOST_URL =
            "https://api.exchangerate.host/convert?from=USD&to=NGN&amount=1";

    @Cacheable(value = "fxRates", key = "'suiToNgn'", unless = "#result == null")
    @Override
    public BigDecimal getSuiToNgnRate() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(COINGECKO_URL, Map.class);
            Map<String, Object> body = response.getBody();
            Map<String, Object> sui = (Map<String, Object>) body.get("sui");
            return BigDecimal.valueOf(((Number) sui.get("ngn")).doubleValue());
        } catch (Exception e) {
            log.error("Failed to fetch SUI->NGN rate, defaulting to 1500", e);
            return BigDecimal.valueOf(1500);
        }
    }

    @Cacheable(value = "fxRates", key = "'usdToNgn'", unless = "#result == null")
    @Override
    public BigDecimal getUsdToNgnRate() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(EXCHANGE_RATE_HOST_URL, Map.class);
            Map<String, Object> body = response.getBody();
            return BigDecimal.valueOf(((Number) body.get("result")).doubleValue());
        } catch (Exception e) {
            log.error("Failed to fetch USD->NGN rate, defaulting to 1600", e);
            return BigDecimal.valueOf(1600);
        }
    }

    @Override
    public BigDecimal convertToFiat(BigDecimal amount, WalletType walletType) {
        switch (walletType) {
            case SUI:
                return amount.multiply(getSuiToNgnRate());
            case NGN:
                return amount;
            default:
                throw new IllegalArgumentException("Unsupported wallet type: " + walletType);
        }
    }

    @Override
    public BigDecimal convertToSui(BigDecimal amount, WalletType walletType) {
        switch (walletType) {
            case NGN:
                return amount.divide(getSuiToNgnRate(), 6, RoundingMode.HALF_UP);
            case SUI:
                return amount;
            default:
                throw new IllegalArgumentException("Unsupported wallet type: " + walletType);
        }
    }
}

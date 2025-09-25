package com.clyrafy.wallet.wallet.enums;

import lombok.Getter;

@Getter
public enum WalletType {

    // ===== FIAT: African =====
    NGN(WalletStatus.FIAT),   // Nigerian Naira
    GHS(WalletStatus.FIAT),   // Ghanaian Cedi
    KES(WalletStatus.FIAT),   // Kenyan Shilling
    ZAR(WalletStatus.FIAT),   // South African Rand
    XAF(WalletStatus.FIAT),   // Central African CFA Franc
    XOF(WalletStatus.FIAT),   // West African CFA Franc
    EGP(WalletStatus.FIAT),   // Egyptian Pound
    MAD(WalletStatus.FIAT),   // Moroccan Dirham
    TZS(WalletStatus.FIAT),   // Tanzanian Shilling
    UGX(WalletStatus.FIAT),   // Ugandan Shilling
    SDG(WalletStatus.FIAT),   // Sudanese Pound
    DZD(WalletStatus.FIAT),   // Algerian Dinar
    ETB(WalletStatus.FIAT),   // Ethiopian Birr
    RWF(WalletStatus.FIAT),   // Rwandan Franc
    MWK(WalletStatus.FIAT),   // Malawian Kwacha
    ZMW(WalletStatus.FIAT),   // Zambian Kwacha
    BWP(WalletStatus.FIAT),   // Botswana Pula
    MZN(WalletStatus.FIAT),   // Mozambican Metical
    LRD(WalletStatus.FIAT),   // Liberian Dollar
    SLL(WalletStatus.FIAT),   // Sierra Leone an Leone
    GMD(WalletStatus.FIAT),   // Gambian Dalasi
    MRU(WalletStatus.FIAT),   // Mauritanian Ouguiya
    SCR(WalletStatus.FIAT),   // Seychellois Rupee

    // ===== FIAT: Global =====
    USD(WalletStatus.FIAT),   // US Dollar
    EUR(WalletStatus.FIAT),   // Euro
    GBP(WalletStatus.FIAT),   // British Pound
    JPY(WalletStatus.FIAT),   // Japanese Yen
    CNY(WalletStatus.FIAT),   // Chinese Yuan
    INR(WalletStatus.FIAT),   // Indian Rupee
    CAD(WalletStatus.FIAT),   // Canadian Dollar
    AUD(WalletStatus.FIAT),   // Australian Dollar
    BRL(WalletStatus.FIAT),   // Brazilian Real
    MXN(WalletStatus.FIAT),   // Mexican Peso
    CHF(WalletStatus.FIAT),   // Swiss Franc
    SEK(WalletStatus.FIAT),   // Swedish Króna
    NOK(WalletStatus.FIAT),   // Norwegian Krone
    AED(WalletStatus.FIAT),   // UAE Durham
    SAR(WalletStatus.FIAT),   // Saudi Riyal
    TRY(WalletStatus.FIAT),   // Turkish Lira
    KRW(WalletStatus.FIAT),   // South Korean Won

    // ===== CRYPTO =====
    SUI(WalletStatus.CRYPTO),
    BTC(WalletStatus.CRYPTO),
    ETH(WalletStatus.CRYPTO),
    USDT(WalletStatus.CRYPTO),
    USDC(WalletStatus.CRYPTO),
    BNB(WalletStatus.CRYPTO),
    SOL(WalletStatus.CRYPTO),
    MATIC(WalletStatus.CRYPTO),
    XRP(WalletStatus.CRYPTO),
    TRX(WalletStatus.CRYPTO);

    private final WalletStatus walletStatus;

    WalletType(WalletStatus walletStatus) {
        this.walletStatus = walletStatus;
    }
}

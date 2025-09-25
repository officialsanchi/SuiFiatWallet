package com.clyrafy.wallet.transaction.enums;

public enum TransactionType {
    DEPOSIT, // Top-up from external source (e.g., Paystack) into the pool, user gets credited
    WITHDRAWAL, // User requests payout from pool to their external account
    TRANSFER, // Internal transfer from User A to User B (e.g., payment for service)
    FEE // Platform deducting a fee
}
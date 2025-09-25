package com.clyrafy.wallet.transaction.enums;

public enum TransactionStatus {
    PENDING,    // Created but not yet processed
    PROCESSING, // Currently being processed
    SUCCESS,    // Completed successfully
    FAILED,     // Failed permanently
    CANCELLED
}

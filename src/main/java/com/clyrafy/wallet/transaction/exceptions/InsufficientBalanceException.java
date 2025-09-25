package com.clyrafy.wallet.transaction.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class InsufficientBalanceException extends GlassWalletException {
    public InsufficientBalanceException(String message) {
        super(message);

    }
}

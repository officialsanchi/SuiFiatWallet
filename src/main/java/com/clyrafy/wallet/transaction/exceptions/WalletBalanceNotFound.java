package com.clyrafy.wallet.transaction.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class WalletBalanceNotFound extends GlassWalletException {
    public WalletBalanceNotFound(String message) {
        super(message);
    }
}

package com.clyrafy.wallet.wallet.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class WalletNotFoundException extends GlassWalletException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}

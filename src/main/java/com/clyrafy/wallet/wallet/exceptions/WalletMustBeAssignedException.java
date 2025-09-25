package com.clyrafy.wallet.wallet.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class WalletMustBeAssignedException extends GlassWalletException {
    public WalletMustBeAssignedException(String message) {
        super(message);
    }
}

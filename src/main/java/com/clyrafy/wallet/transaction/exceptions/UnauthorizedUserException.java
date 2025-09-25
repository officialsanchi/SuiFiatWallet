package com.clyrafy.wallet.transaction.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class UnauthorizedUserException extends GlassWalletException {
    public UnauthorizedUserException(String message) {
        super(message);
    }
}

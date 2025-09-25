package com.clyrafy.wallet.commons.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class PinMismatchException extends GlassWalletException {
    public PinMismatchException(String message) {
        super(message);
    }
}

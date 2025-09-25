package com.clyrafy.wallet.commons.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class WrongWalletException extends GlassWalletException {
    public WrongWalletException(String message) {
        super(message);
    }
}

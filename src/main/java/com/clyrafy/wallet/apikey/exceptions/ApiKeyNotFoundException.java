package com.clyrafy.wallet.apikey.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class ApiKeyNotFoundException extends GlassWalletException {
    public ApiKeyNotFoundException(String message) {
        super(message);
    }
}

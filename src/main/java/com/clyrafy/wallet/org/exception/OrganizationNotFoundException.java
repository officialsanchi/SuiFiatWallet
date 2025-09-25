package com.clyrafy.wallet.org.exception;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class OrganizationNotFoundException extends GlassWalletException {
    public OrganizationNotFoundException(String message) {
        super(message);
    }
}

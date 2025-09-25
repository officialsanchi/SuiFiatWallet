package com.clyrafy.wallet.exceptions;

public class UserAlreadyExistsException extends GlassWalletException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

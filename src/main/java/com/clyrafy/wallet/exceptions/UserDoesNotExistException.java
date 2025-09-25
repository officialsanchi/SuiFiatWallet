package com.clyrafy.wallet.exceptions;

public class UserDoesNotExistException extends GlassWalletException{
    public UserDoesNotExistException(String message) {
        super(message);
    }
}

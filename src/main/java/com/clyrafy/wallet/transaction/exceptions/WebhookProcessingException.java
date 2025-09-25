package com.clyrafy.wallet.transaction.exceptions;

import com.clyrafy.wallet.exceptions.GlassWalletException;

public class WebhookProcessingException extends GlassWalletException {
    public WebhookProcessingException(String webhookProcessingFailed, Exception exception) {
        super(webhookProcessingFailed);
    }
}

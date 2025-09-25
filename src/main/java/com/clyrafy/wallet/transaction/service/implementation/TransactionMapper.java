package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.transaction.data.models.Transaction;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.enums.WalletType;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponse response = new TransactionResponse();
        response.setReference(transaction.getReference());
        response.setType(determineWalletType(transaction)); // Custom logic to set WalletType
        response.setStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null);
        response.setTxHash(transaction.getTxHash());
        response.setAmount(transaction.getAmount());
        response.setSender(transaction.getSenderWallet());
        response.setRecipient(transaction.getReceiverWallet());

        // These can be set manually in the service if needed
        // response.setSenderBalance(...);
        // response.setRecipientBalance(...);

        return response;
    }

    private WalletType determineWalletType(Transaction transaction) {
        if (transaction == null || transaction.getWallet() == null || transaction.getWallet().getBalances() == null) {
            return WalletType.NGN; // Default to NGN if no data
        }
        return transaction.getWallet().getBalances().stream()
                .findFirst()
                .map(WalletBalance::getCurrencyType)
                .orElse(WalletType.NGN); // Default to NGN if no balance
    }
}
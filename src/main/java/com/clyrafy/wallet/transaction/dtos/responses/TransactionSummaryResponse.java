package com.clyrafy.wallet.transaction.dtos.responses;

import com.clyrafy.wallet.transaction.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
 public class TransactionSummaryResponse {
    private String reference;
    private String txHash;
    private BigDecimal amount;
    private TransactionStatus status;
    private String senderWallet;
    private String receiverWallet;
    private LocalDateTime createdAt;

    public TransactionSummaryResponse(String reference, String txHash, BigDecimal amount, TransactionStatus status,
                                      String senderWallet, String receiverWallet, LocalDateTime createdAt) {
        this.reference = reference;
        this.txHash = txHash;
        this.amount = amount;
        this.status = status;
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.createdAt = createdAt;
    }
}
package com.clyrafy.wallet.transaction.data.models;

import com.clyrafy.wallet.transaction.enums.TransactionStatus;
import com.clyrafy.wallet.transaction.enums.TransactionType;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "tx_hash")
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletType walletType;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "sender_wallet")
    private String senderWallet;

    @Column(name = "receiver_wallet")
    private String receiverWallet;

    @Column(name = "purpose_of_tnx")
    private String reason;

    private String memo;

    @Column(name = "bulk_reference")
    private String bulkReference;

    @Column(name = "sender_balance_snapshot")
    private BigDecimal senderBalanceSnapshot;

    @Column(name = "recipient_balance_snapshot")
    private BigDecimal recipientBalanceSnapshot;

    private LocalDateTime createdAt = LocalDateTime.now();

}
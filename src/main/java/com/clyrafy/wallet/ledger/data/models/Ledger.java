package com.clyrafy.wallet.ledger.data.models;

import com.clyrafy.wallet.wallet.enums.WalletType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    private String transactionType;
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private WalletType walletType;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name="tx_hash")
    private String txHash;

    @Column(name="owner_type")
    private String ownerType;

    @Column(name="receiver_address")
    private String receiverAddress;

    @Column(name = "initiator_type")
    private String initiatorType;

    @Column(name = "initiator_id")
    private UUID initiatorId;


}

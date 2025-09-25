package com.clyrafy.wallet.wallet.data.models;

import com.clyrafy.wallet.wallet.enums.WalletType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "wallet_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "currency_type"})
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type")
    private WalletType currencyType;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
}

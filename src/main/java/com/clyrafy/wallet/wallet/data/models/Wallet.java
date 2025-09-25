package com.clyrafy.wallet.wallet.data.models;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.exceptions.WalletMustBeAssignedException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wallet_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_user_id")
    private EndUser endUser;

    @Column(name = "wallet_type")
    @Enumerated(EnumType.STRING)
    private WalletType walletType;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WalletBalance> balances = new HashSet<>();

    @Column(name = "virtual_acc_num", unique = true)
    private String virtualAccountNum;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void validateWallet() {
        if (endUser == null && organization == null) {
            throw new WalletMustBeAssignedException("Wallet must be tied to an organization or end user.");
        }
    }


}

package com.clyrafy.wallet.wallet.data.models;

import com.clyrafy.wallet.org.data.models.User;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "org_user_wallet_access")
public class OrgUserWalletAccess {

    @EmbeddedId
    private OrgUserWalletId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orgUserId")
    @JoinColumn(name = "org_user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("walletId")
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false)
    private boolean isAdmin;

}

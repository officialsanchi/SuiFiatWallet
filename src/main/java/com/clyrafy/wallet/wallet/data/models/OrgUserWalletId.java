package com.clyrafy.wallet.wallet.data.models;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrgUserWalletId implements Serializable {
    private UUID orgUserId;
    private UUID walletId;

}

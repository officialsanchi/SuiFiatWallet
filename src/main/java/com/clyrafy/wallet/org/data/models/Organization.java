package com.clyrafy.wallet.org.data.models;

import com.clyrafy.wallet.apikey.data.models.ApiKey;
import com.clyrafy.wallet.kyb.enums.KybStatus;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "organization_id")
    private UUID id;

    @Column(name = "generated_org_id", nullable = false, unique = true)
    private String orgId;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KybStatus kybStatus = KybStatus.PENDING;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wallet> wallets = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApiKey> apiKeys = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> staff = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addApiKey(ApiKey key) {
        apiKeys.add(key);
        key.setOrganization(this);
    }

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        wallet.setOrganization(this);
    }
}

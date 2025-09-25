package com.clyrafy.wallet.enduser.data.models;

import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "end_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String virtualAccountNumber;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.END_USER;

    @Column(name = "is_kyc_verified")
    private boolean isKycVerified;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "endUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Wallet> wallets = new HashSet<>();


}

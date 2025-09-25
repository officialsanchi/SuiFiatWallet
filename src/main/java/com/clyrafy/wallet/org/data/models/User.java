package com.clyrafy.wallet.org.data.models;

import com.clyrafy.wallet.org.enums.OrgStatus;
import com.clyrafy.wallet.org.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "organization_admin_id")
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ORG_ADMIN;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(nullable = false)
    private boolean isLoggedIn;

    @Column(name = "country")
    private String country;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "email_verification_status")
    private OrgStatus emailVerificationStatus = OrgStatus.PENDING_VERIFICATION;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean acceptTerms;

    public boolean isEmailVerified() {
        return emailVerificationStatus == OrgStatus.ACTIVE;
    }
}

package com.clyrafy.wallet.apikey.data.models;

import com.clyrafy.wallet.apikey.enums.ApiKeyStatus;
import com.clyrafy.wallet.apikey.enums.ApiKeyType;
import com.clyrafy.wallet.org.data.models.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String publicKey;

    @Column(nullable = false)
    private String secretKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyType type;

    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiredAt;

    @Column(name = "is_primary")
    private boolean primary = false;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}

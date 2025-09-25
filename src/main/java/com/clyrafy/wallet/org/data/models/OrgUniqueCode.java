package com.clyrafy.wallet.org.data.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_unique_codes")
@Getter
@Setter
public class OrgUniqueCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CodeStatus status = CodeStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public enum CodeStatus { AVAILABLE, USED }

    public OrgUniqueCode() {}

    public OrgUniqueCode(String code, String orgId) {
        this.code = code;
        this.orgId = orgId;
    }
}
package com.loris.onebrain.coupon.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "coupon")
public class CouponJpaEntity {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "redeemed", nullable = false)
    private boolean redeemed;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected CouponJpaEntity() {
    }

    public CouponJpaEntity(UUID id,
                           String code,
                           String description,
                           BigDecimal discountValue,
                           Instant expirationDate,
                           boolean published,
                           boolean redeemed,
                           Instant deletedAt,
                           Long version) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.redeemed = redeemed;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Long getVersion() {
        return version;
    }
}

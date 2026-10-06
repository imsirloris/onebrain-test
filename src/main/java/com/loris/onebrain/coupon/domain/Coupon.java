package com.loris.onebrain.coupon.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class Coupon {

    private final UUID id;
    private final CouponCode code;
    private final CouponDescription description;
    private final DiscountValue discountValue;
    private final Instant expirationDate;
    private final boolean published;
    private final boolean redeemed;
    private Instant deletedAt;
    private final Long version;

    private Coupon(UUID id,
                   CouponCode code,
                   CouponDescription description,
                   DiscountValue discountValue,
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

    public static Coupon create(String code,
                                String description,
                                BigDecimal discountValue,
                                Instant expirationDate,
                                Boolean published,
                                Instant now) {
        CouponCode couponCode = new CouponCode(code);
        CouponDescription couponDescription = new CouponDescription(description);
        DiscountValue couponDiscountValue = new DiscountValue(discountValue);
        if (expirationDate == null) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_EXPIRATION_DATE_REQUIRED);
        }
        if (expirationDate.isBefore(now)) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_EXPIRATION_DATE_IN_PAST);
        }
        return new Coupon(UUID.randomUUID(),
                couponCode,
                couponDescription,
                couponDiscountValue,
                expirationDate,
                Boolean.TRUE.equals(published),
                false,
                null,
                null);
    }

    public static Coupon restore(UUID id,
                                 String code,
                                 String description,
                                 BigDecimal discountValue,
                                 Instant expirationDate,
                                 boolean published,
                                 boolean redeemed,
                                 Instant deletedAt,
                                 Long version) {
        return new Coupon(id,
                new CouponCode(code),
                new CouponDescription(description),
                new DiscountValue(discountValue),
                expirationDate,
                published,
                redeemed,
                deletedAt,
                version);
    }

    public void delete(Instant now) {
        if (isDeleted()) {
            throw new CouponAlreadyDeletedException(id);
        }
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public CouponStatus status() {
        return isDeleted() ? CouponStatus.DELETED : CouponStatus.ACTIVE;
    }

    public UUID id() {
        return id;
    }

    public CouponCode code() {
        return code;
    }

    public CouponDescription description() {
        return description;
    }

    public DiscountValue discountValue() {
        return discountValue;
    }

    public Instant expirationDate() {
        return expirationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public Optional<Instant> deletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public Long version() {
        return version;
    }
}

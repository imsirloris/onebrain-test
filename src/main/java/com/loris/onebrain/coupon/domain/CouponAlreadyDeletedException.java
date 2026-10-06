package com.loris.onebrain.coupon.domain;

import java.util.UUID;

public class CouponAlreadyDeletedException extends DomainException {

    private final UUID couponId;

    public CouponAlreadyDeletedException(UUID couponId) {
        super(CouponErrorCode.COUPON_ALREADY_DELETED);
        this.couponId = couponId;
    }

    public UUID couponId() {
        return couponId;
    }
}

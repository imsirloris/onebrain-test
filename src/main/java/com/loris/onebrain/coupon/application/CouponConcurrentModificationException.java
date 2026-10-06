package com.loris.onebrain.coupon.application;

import java.util.UUID;

public class CouponConcurrentModificationException extends RuntimeException {

    private final UUID couponId;

    public CouponConcurrentModificationException(UUID couponId) {
        super("Coupon was modified concurrently. Retry the operation.");
        this.couponId = couponId;
    }

    public UUID couponId() {
        return couponId;
    }
}

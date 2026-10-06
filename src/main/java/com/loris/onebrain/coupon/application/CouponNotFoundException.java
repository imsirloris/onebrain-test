package com.loris.onebrain.coupon.application;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

    private final UUID couponId;

    public CouponNotFoundException(UUID couponId) {
        super("Coupon not found.");
        this.couponId = couponId;
    }

    public UUID couponId() {
        return couponId;
    }
}

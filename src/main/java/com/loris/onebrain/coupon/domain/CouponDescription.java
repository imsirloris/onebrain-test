package com.loris.onebrain.coupon.domain;

public record CouponDescription(String value) {

    public CouponDescription {
        if (value == null || value.isBlank()) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_DESCRIPTION_REQUIRED);
        }
        value = value.strip();
    }
}

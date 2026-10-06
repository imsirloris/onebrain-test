package com.loris.onebrain.coupon.domain;

import java.math.BigDecimal;

public record DiscountValue(BigDecimal value) {

    public static final BigDecimal MINIMUM = new BigDecimal("0.5");

    public DiscountValue {
        if (value == null) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_DISCOUNT_VALUE_REQUIRED);
        }
        if (value.compareTo(MINIMUM) < 0) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_DISCOUNT_VALUE_BELOW_MINIMUM);
        }
    }
}

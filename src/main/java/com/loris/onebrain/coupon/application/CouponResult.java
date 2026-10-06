package com.loris.onebrain.coupon.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loris.onebrain.coupon.domain.Coupon;

public record CouponResult(UUID id,
                           String code,
                           String description,
                           BigDecimal discountValue,
                           Instant expirationDate,
                           String status,
                           boolean published,
                           boolean redeemed) {

    public static CouponResult from(Coupon coupon) {
        return new CouponResult(coupon.id(),
                coupon.code().value(),
                coupon.description().value(),
                coupon.discountValue().value(),
                coupon.expirationDate(),
                coupon.status().name(),
                coupon.isPublished(),
                coupon.isRedeemed());
    }
}

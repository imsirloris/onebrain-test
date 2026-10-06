package com.loris.onebrain.coupon.application.port;

import java.util.Optional;
import java.util.UUID;

import com.loris.onebrain.coupon.application.CouponConcurrentModificationException;
import com.loris.onebrain.coupon.domain.Coupon;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(UUID id);
}

package com.loris.onebrain.coupon.application;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponCommand(String code,
                                  String description,
                                  BigDecimal discountValue,
                                  Instant expirationDate,
                                  Boolean published) {
}

package com.loris.onebrain.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loris.onebrain.coupon.application.CouponResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CouponResponse")
public record CouponResponse(
        @Schema(example = "cef9d1e3-aae5-4ab6-a297-358c6032b1e7") UUID id,
        @Schema(example = "ABC123") String code,
        @Schema(example = "Black Friday") String description,
        @Schema(example = "0.8") BigDecimal discountValue,
        @Schema(example = "2030-11-04T17:14:45.180Z") Instant expirationDate,
        @Schema(allowableValues = {"ACTIVE", "INACTIVE", "DELETED"}, example = "ACTIVE") String status,
        @Schema(example = "false") boolean published,
        @Schema(example = "false") boolean redeemed) {

    public static CouponResponse from(CouponResult result) {
        return new CouponResponse(result.id(),
                result.code(),
                result.description(),
                result.discountValue(),
                result.expirationDate(),
                result.status(),
                result.published(),
                result.redeemed());
    }
}

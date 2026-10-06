package com.loris.onebrain.coupon.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

import com.loris.onebrain.coupon.application.CreateCouponCommand;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateCouponRequest")
public record CreateCouponRequest(
        @Schema(description = "Coupon code. Special characters are removed; 6 alphanumeric characters must remain.",
                example = "ABC-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Coupon description.", example = "Black Friday",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @Schema(description = "Discount value. Minimum 0.5, no maximum.", example = "0.8",
                requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal discountValue,

        @Schema(description = "Expiration date (ISO-8601, UTC). Cannot be in the past.",
                example = "2030-11-04T17:14:45.180Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant expirationDate,

        @Schema(description = "Whether the coupon is created already published. Defaults to false.",
                example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean published) {

    public CreateCouponCommand toCommand() {
        return new CreateCouponCommand(code, description, discountValue, expirationDate, published);
    }
}

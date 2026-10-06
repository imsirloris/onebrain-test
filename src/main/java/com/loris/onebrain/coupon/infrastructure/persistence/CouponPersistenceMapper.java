package com.loris.onebrain.coupon.infrastructure.persistence;

import java.math.BigDecimal;

import com.loris.onebrain.coupon.domain.Coupon;

final class CouponPersistenceMapper {

    private CouponPersistenceMapper() {
    }

    static CouponJpaEntity toEntity(Coupon coupon) {
        return new CouponJpaEntity(coupon.id(),
                coupon.code().value(),
                coupon.description().value(),
                coupon.discountValue().value(),
                coupon.expirationDate(),
                coupon.isPublished(),
                coupon.isRedeemed(),
                coupon.deletedAt().orElse(null),
                coupon.version());
    }

    static Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.restore(entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                normalizeScale(entity.getDiscountValue()),
                entity.getExpirationDate(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.getDeletedAt(),
                entity.getVersion());
    }

    private static BigDecimal normalizeScale(BigDecimal value) {
        return new BigDecimal(value.stripTrailingZeros().toPlainString());
    }
}

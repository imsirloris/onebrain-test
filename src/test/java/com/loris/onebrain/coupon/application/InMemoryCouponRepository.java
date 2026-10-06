package com.loris.onebrain.coupon.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.loris.onebrain.coupon.application.port.CouponRepository;
import com.loris.onebrain.coupon.domain.Coupon;

class InMemoryCouponRepository implements CouponRepository {

    private final Map<UUID, Coupon> coupons = new HashMap<>();
    private int saveCount;

    @Override
    public Coupon save(Coupon coupon) {
        Coupon stored = coupons.get(coupon.id());
        Long storedVersion = stored == null ? null : stored.version();
        if (!Objects.equals(storedVersion, coupon.version())) {
            throw new CouponConcurrentModificationException(coupon.id());
        }
        Long nextVersion = storedVersion == null ? 0L : storedVersion + 1;
        Coupon copy = copy(coupon, nextVersion);
        coupons.put(copy.id(), copy);
        saveCount++;
        return copy(copy, nextVersion);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return Optional.ofNullable(coupons.get(id)).map(coupon -> copy(coupon, coupon.version()));
    }

    int saveCount() {
        return saveCount;
    }

    int size() {
        return coupons.size();
    }

    private static Coupon copy(Coupon coupon, Long version) {
        return Coupon.restore(coupon.id(),
                coupon.code().value(),
                coupon.description().value(),
                coupon.discountValue().value(),
                coupon.expirationDate(),
                coupon.isPublished(),
                coupon.isRedeemed(),
                coupon.deletedAt().orElse(null),
                version);
    }
}

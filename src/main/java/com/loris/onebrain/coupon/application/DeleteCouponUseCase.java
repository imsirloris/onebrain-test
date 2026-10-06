package com.loris.onebrain.coupon.application;

import java.time.Clock;
import java.util.UUID;

import com.loris.onebrain.coupon.application.port.CouponRepository;
import com.loris.onebrain.coupon.domain.Coupon;

public class DeleteCouponUseCase {

    private final CouponRepository repository;
    private final Clock clock;

    public DeleteCouponUseCase(CouponRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void execute(UUID id) {
        Coupon coupon = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
        coupon.delete(clock.instant());
        repository.save(coupon);
    }
}

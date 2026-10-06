package com.loris.onebrain.coupon.application;

import java.time.Clock;

import com.loris.onebrain.coupon.application.port.CouponRepository;
import com.loris.onebrain.coupon.domain.Coupon;

public class CreateCouponUseCase {

    private final CouponRepository repository;
    private final Clock clock;

    public CreateCouponUseCase(CouponRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public CouponResult execute(CreateCouponCommand command) {
        Coupon coupon = Coupon.create(command.code(),
                command.description(),
                command.discountValue(),
                command.expirationDate(),
                command.published(),
                clock.instant());
        return CouponResult.from(repository.save(coupon));
    }
}

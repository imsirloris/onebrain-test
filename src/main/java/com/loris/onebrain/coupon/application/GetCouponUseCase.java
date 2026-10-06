package com.loris.onebrain.coupon.application;

import java.util.UUID;

import com.loris.onebrain.coupon.application.port.CouponRepository;

public class GetCouponUseCase {

    private final CouponRepository repository;

    public GetCouponUseCase(CouponRepository repository) {
        this.repository = repository;
    }

    public CouponResult execute(UUID id) {
        return repository.findById(id)
                .map(CouponResult::from)
                .orElseThrow(() -> new CouponNotFoundException(id));
    }
}

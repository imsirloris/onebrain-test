package com.loris.onebrain.coupon.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCouponRepository extends JpaRepository<CouponJpaEntity, UUID> {
}

package com.loris.onebrain.coupon.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.loris.onebrain.coupon.application.CouponConcurrentModificationException;
import com.loris.onebrain.coupon.application.port.CouponRepository;
import com.loris.onebrain.coupon.domain.Coupon;

@Repository
public class JpaCouponRepository implements CouponRepository {

    private final SpringDataCouponRepository springDataRepository;

    @Autowired
    public JpaCouponRepository(SpringDataCouponRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        try {
            CouponJpaEntity saved = springDataRepository.saveAndFlush(CouponPersistenceMapper.toEntity(coupon));
            return CouponPersistenceMapper.toDomain(saved);
        } catch (OptimisticLockingFailureException exception) {
            throw new CouponConcurrentModificationException(coupon.id());
        }
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return springDataRepository.findById(id).map(CouponPersistenceMapper::toDomain);
    }
}

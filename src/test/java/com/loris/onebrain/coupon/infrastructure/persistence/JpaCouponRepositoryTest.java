package com.loris.onebrain.coupon.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.loris.onebrain.coupon.application.CouponConcurrentModificationException;
import com.loris.onebrain.coupon.domain.Coupon;
import com.loris.onebrain.coupon.domain.CouponStatus;

@DataJpaTest
@Import(JpaCouponRepository.class)
@DisplayName("JpaCouponRepository")
class JpaCouponRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant EXPIRATION = Instant.parse("2030-11-04T17:14:45.180Z");

    @Autowired
    private JpaCouponRepository repository;

    @Autowired
    private SpringDataCouponRepository springDataRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("persists every field and returns the coupon with its first version")
    void savesAndLoadsCoupon() {
        Coupon coupon = newCoupon("0.8", true);

        Coupon saved = repository.save(coupon);
        entityManager.clear();
        Coupon loaded = repository.findById(coupon.id()).orElseThrow();

        assertThat(saved.version()).isNotNull();
        assertThat(loaded.id()).isEqualTo(coupon.id());
        assertThat(loaded.code().value()).isEqualTo("ABC123");
        assertThat(loaded.description().value()).isEqualTo("Black Friday");
        assertThat(loaded.expirationDate()).isEqualTo(EXPIRATION);
        assertThat(loaded.isPublished()).isTrue();
        assertThat(loaded.isRedeemed()).isFalse();
        assertThat(loaded.deletedAt()).isEmpty();
        assertThat(loaded.status()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(loaded.version()).isEqualTo(saved.version());
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("D-11: the database scale is stripped when loading (0.8000 -> 0.8)")
    void normalizesDiscountScale() {
        Coupon coupon = newCoupon("0.8", false);
        repository.save(coupon);
        entityManager.clear();

        Coupon loaded = repository.findById(coupon.id()).orElseThrow();

        assertThat(loaded.discountValue().value()).isEqualTo(new BigDecimal("0.8"));
    }

    @Test
    @DisplayName("D-11: integer discounts come back without exponent notation")
    void normalizesIntegerDiscountWithoutExponent() {
        Coupon coupon = newCoupon("100", false);
        repository.save(coupon);
        entityManager.clear();

        Coupon loaded = repository.findById(coupon.id()).orElseThrow();

        assertThat(loaded.discountValue().value()).isEqualTo(new BigDecimal("100"));
        assertThat(loaded.discountValue().value().toString()).isEqualTo("100");
    }

    @Test
    @DisplayName("D-11 known limitation: the database rounds beyond 4 decimal places")
    void roundsBeyondFourDecimalPlaces() {
        Coupon coupon = newCoupon("0.55555", false);
        repository.save(coupon);
        entityManager.clear();

        Coupon loaded = repository.findById(coupon.id()).orElseThrow();

        assertThat(loaded.discountValue().value()).isEqualTo(new BigDecimal("0.5556"));
    }

    @Test
    @DisplayName("RN-11: delete is a soft delete, the row and its data are kept")
    void persistsSoftDelete() {
        Coupon saved = repository.save(newCoupon("0.8", false));
        entityManager.clear();
        Instant deletionTime = NOW.plusSeconds(60);

        saved.delete(deletionTime);
        Coupon deleted = repository.save(saved);
        entityManager.clear();
        Coupon loaded = repository.findById(saved.id()).orElseThrow();

        assertThat(springDataRepository.count()).isEqualTo(1);
        assertThat(deleted.version()).isGreaterThan(saved.version());
        assertThat(loaded.status()).isEqualTo(CouponStatus.DELETED);
        assertThat(loaded.deletedAt()).contains(deletionTime);
        assertThat(loaded.code().value()).isEqualTo("ABC123");
        assertThat(loaded.description().value()).isEqualTo("Black Friday");
    }

    @Test
    @DisplayName("D-13: saving a stale version raises a concurrent modification")
    void rejectsStaleVersion() {
        Coupon saved = repository.save(newCoupon("0.8", false));
        entityManager.clear();
        Coupon firstReader = repository.findById(saved.id()).orElseThrow();
        Coupon secondReader = repository.findById(saved.id()).orElseThrow();
        entityManager.clear();

        firstReader.delete(NOW);
        repository.save(firstReader);
        entityManager.clear();
        secondReader.delete(NOW.plusSeconds(1));

        assertThatThrownBy(() -> repository.save(secondReader))
                .isInstanceOfSatisfying(CouponConcurrentModificationException.class,
                        exception -> assertThat(exception.couponId()).isEqualTo(saved.id()));
    }

    private static Coupon newCoupon(String discount, boolean published) {
        return Coupon.create("ABC-123", "Black Friday", new BigDecimal(discount), EXPIRATION, published, NOW);
    }
}

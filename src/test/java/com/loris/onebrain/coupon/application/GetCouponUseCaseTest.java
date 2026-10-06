package com.loris.onebrain.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.loris.onebrain.coupon.domain.Coupon;

@DisplayName("GetCouponUseCase")
class GetCouponUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private InMemoryCouponRepository repository;
    private GetCouponUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCouponRepository();
        useCase = new GetCouponUseCase(repository);
    }

    @Test
    void returnsExistingCoupon() {
        Coupon saved = repository.save(newCoupon());

        CouponResult result = useCase.execute(saved.id());

        assertThat(result.id()).isEqualTo(saved.id());
        assertThat(result.code()).isEqualTo("ABC123");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("RN-13: a deleted coupon is still returned, with status DELETED")
    void returnsDeletedCoupon() {
        Coupon coupon = repository.save(newCoupon());
        coupon.delete(NOW);
        repository.save(coupon);

        CouponResult result = useCase.execute(coupon.id());

        assertThat(result.status()).isEqualTo("DELETED");
        assertThat(result.code()).isEqualTo("ABC123");
        assertThat(result.description()).isEqualTo("Black Friday");
    }

    @Test
    void rejectsUnknownCoupon() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOfSatisfying(CouponNotFoundException.class, exception -> {
                    assertThat(exception.couponId()).isEqualTo(unknownId);
                    assertThat(exception).hasMessage("Coupon not found.");
                });
    }

    private static Coupon newCoupon() {
        return Coupon.create("ABC-123", "Black Friday", new BigDecimal("0.8"), NOW.plusSeconds(3600), false, NOW);
    }
}

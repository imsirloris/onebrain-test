package com.loris.onebrain.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.loris.onebrain.coupon.domain.Coupon;
import com.loris.onebrain.coupon.domain.CouponAlreadyDeletedException;
import com.loris.onebrain.coupon.domain.CouponStatus;

@DisplayName("DeleteCouponUseCase")
class DeleteCouponUseCaseTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant EXPIRATION = CREATED_AT.plus(Duration.ofDays(1));

    private final InMemoryCouponRepository repository = new InMemoryCouponRepository();

    @Test
    @DisplayName("RN-11: soft deletes the coupon and persists deletedAt from the clock")
    void softDeletesCoupon() {
        Coupon saved = repository.save(newCoupon(false));
        Instant deletionTime = CREATED_AT.plusSeconds(60);

        useCaseAt(deletionTime).execute(saved.id());

        Coupon stored = repository.findById(saved.id()).orElseThrow();
        assertThat(stored.status()).isEqualTo(CouponStatus.DELETED);
        assertThat(stored.deletedAt()).contains(deletionTime);
        assertThat(stored.code().value()).isEqualTo("ABC123");
        assertThat(stored.description().value()).isEqualTo("Black Friday");
        assertThat(stored.expirationDate()).isEqualTo(EXPIRATION);
    }

    @Test
    @DisplayName("RN-10: an expired coupon can be deleted")
    void deletesExpiredCoupon() {
        Coupon saved = repository.save(newCoupon(false));

        useCaseAt(EXPIRATION.plus(Duration.ofDays(90))).execute(saved.id());

        assertThat(repository.findById(saved.id()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    @DisplayName("RN-10: a published coupon can be deleted")
    void deletesPublishedCoupon() {
        Coupon saved = repository.save(newCoupon(true));

        useCaseAt(CREATED_AT).execute(saved.id());

        Coupon stored = repository.findById(saved.id()).orElseThrow();
        assertThat(stored.isDeleted()).isTrue();
        assertThat(stored.isPublished()).isTrue();
    }

    @Test
    @DisplayName("RN-12: a deleted coupon cannot be deleted again")
    void rejectsDeletingTwice() {
        Coupon saved = repository.save(newCoupon(false));
        DeleteCouponUseCase useCase = useCaseAt(CREATED_AT.plusSeconds(1));
        useCase.execute(saved.id());
        int savesBefore = repository.saveCount();

        assertThatThrownBy(() -> useCase.execute(saved.id()))
                .isInstanceOf(CouponAlreadyDeletedException.class);
        assertThat(repository.saveCount()).isEqualTo(savesBefore);
        assertThat(repository.findById(saved.id()).orElseThrow().deletedAt()).contains(CREATED_AT.plusSeconds(1));
    }

    @Test
    void rejectsUnknownCoupon() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> useCaseAt(CREATED_AT).execute(unknownId))
                .isInstanceOfSatisfying(CouponNotFoundException.class,
                        exception -> assertThat(exception.couponId()).isEqualTo(unknownId));
    }

    @Test
    @DisplayName("D-13: a stale version surfaces as a concurrent modification")
    void propagatesConcurrentModification() {
        Coupon saved = repository.save(newCoupon(false));
        Coupon staleCopy = repository.findById(saved.id()).orElseThrow();
        useCaseAt(CREATED_AT).execute(saved.id());

        staleCopy.delete(CREATED_AT);

        assertThatThrownBy(() -> repository.save(staleCopy))
                .isInstanceOfSatisfying(CouponConcurrentModificationException.class, exception -> {
                    assertThat(exception.couponId()).isEqualTo(saved.id());
                    assertThat(exception).hasMessage("Coupon was modified concurrently. Retry the operation.");
                });
    }

    private DeleteCouponUseCase useCaseAt(Instant now) {
        return new DeleteCouponUseCase(repository, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static Coupon newCoupon(boolean published) {
        return Coupon.create("ABC-123", "Black Friday", new BigDecimal("0.8"), EXPIRATION, published, CREATED_AT);
    }
}

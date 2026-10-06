package com.loris.onebrain.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.loris.onebrain.coupon.domain.CouponErrorCode;
import com.loris.onebrain.coupon.domain.InvalidCouponException;

@DisplayName("CreateCouponUseCase")
class CreateCouponUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant TOMORROW = Instant.parse("2026-01-16T10:00:00Z");

    private InMemoryCouponRepository repository;
    private CreateCouponUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCouponRepository();
        useCase = new CreateCouponUseCase(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("creates and persists a coupon, returning a result instead of the aggregate")
    void createsAndPersistsCoupon() {
        CouponResult result = useCase.execute(command("ABC-123", true));

        assertThat(result.id()).isNotNull();
        assertThat(result.code()).isEqualTo("ABC123");
        assertThat(result.description()).isEqualTo("Black Friday");
        assertThat(result.discountValue()).isEqualByComparingTo("0.8");
        assertThat(result.expirationDate()).isEqualTo(TOMORROW);
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.published()).isTrue();
        assertThat(result.redeemed()).isFalse();
        assertThat(repository.findById(result.id())).isPresent();
    }

    @Test
    @DisplayName("RN-08: published defaults to false")
    void publishedDefaultsToFalse() {
        CouponResult result = useCase.execute(command("ABC123", null));

        assertThat(result.published()).isFalse();
    }

    @Test
    @DisplayName("RN-07: uses the injected clock to reject expiration dates in the past")
    void usesClockAsNow() {
        CreateCouponCommand expired = new CreateCouponCommand("ABC123", "Black Friday", new BigDecimal("0.8"),
                NOW.minusSeconds(1), false);

        assertThatThrownBy(() -> useCase.execute(expired))
                .isInstanceOfSatisfying(InvalidCouponException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(CouponErrorCode.COUPON_EXPIRATION_DATE_IN_PAST));
        assertThat(repository.size()).isZero();
    }

    @Test
    @DisplayName("RN-07: an expiration date equal to the clock instant is accepted")
    void acceptsExpirationEqualToNow() {
        CreateCouponCommand expiringNow = new CreateCouponCommand("ABC123", "Black Friday", new BigDecimal("0.8"),
                NOW, false);

        assertThat(useCase.execute(expiringNow).expirationDate()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("D-07: codes are not unique")
    void allowsDuplicatedCodes() {
        CouponResult first = useCase.execute(command("ABC123", false));
        CouponResult second = useCase.execute(command("abc-123", false));

        assertThat(second.code()).isEqualTo(first.code());
        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(repository.size()).isEqualTo(2);
    }

    private static CreateCouponCommand command(String code, Boolean published) {
        return new CreateCouponCommand(code, "Black Friday", new BigDecimal("0.8"), TOMORROW, published);
    }
}

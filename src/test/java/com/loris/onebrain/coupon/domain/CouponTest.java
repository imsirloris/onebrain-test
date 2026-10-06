package com.loris.onebrain.coupon.domain;

import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_CODE_INVALID_LENGTH;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_CODE_REQUIRED;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DESCRIPTION_REQUIRED;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DISCOUNT_VALUE_BELOW_MINIMUM;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DISCOUNT_VALUE_REQUIRED;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_EXPIRATION_DATE_IN_PAST;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_EXPIRATION_DATE_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

@DisplayName("Coupon")
class CouponTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant TOMORROW = NOW.plus(Duration.ofDays(1));
    private static final BigDecimal DISCOUNT = new BigDecimal("0.8");

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("RN-02 / RN-09: a valid coupon is created ACTIVE, not redeemed and with a sanitized code")
        void createsValidCoupon() {
            Coupon coupon = Coupon.create("ABC-123", "  Black Friday ", DISCOUNT, TOMORROW, false, NOW);

            assertThat(coupon.id()).isNotNull();
            assertThat(coupon.code().value()).isEqualTo("ABC123");
            assertThat(coupon.description().value()).isEqualTo("Black Friday");
            assertThat(coupon.discountValue().value()).isEqualTo(DISCOUNT);
            assertThat(coupon.expirationDate()).isEqualTo(TOMORROW);
            assertThat(coupon.status()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(coupon.isRedeemed()).isFalse();
            assertThat(coupon.isDeleted()).isFalse();
            assertThat(coupon.deletedAt()).isEmpty();
            assertThat(coupon.version()).isNull();
        }

        @Test
        void generatesADifferentIdForEachCoupon() {
            Coupon first = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, false, NOW);
            Coupon second = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, false, NOW);

            assertThat(first.id()).isNotEqualTo(second.id());
        }

        @Test
        @DisplayName("RN-08: can be created already published")
        void canBeCreatedPublished() {
            Coupon coupon = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, true, NOW);

            assertThat(coupon.isPublished()).isTrue();
        }

        @Test
        @DisplayName("RN-08: published defaults to false when omitted")
        void publishedDefaultsToFalse() {
            Coupon coupon = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, null, NOW);

            assertThat(coupon.isPublished()).isFalse();
        }

        @Test
        @DisplayName("RN-07: an expiration date equal to now is accepted")
        void acceptsExpirationDateEqualToNow() {
            Coupon coupon = Coupon.create("ABC123", "Black Friday", DISCOUNT, NOW, false, NOW);

            assertThat(coupon.expirationDate()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("RN-07: an expiration date in the past is rejected")
        void rejectsExpirationDateInThePast() {
            Instant oneMillisecondAgo = NOW.minusMillis(1);

            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", DISCOUNT, oneMillisecondAgo, false, NOW),
                    COUPON_EXPIRATION_DATE_IN_PAST);
        }

        @Test
        @DisplayName("RN-01: expiration date is required")
        void rejectsMissingExpirationDate() {
            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", DISCOUNT, null, false, NOW),
                    COUPON_EXPIRATION_DATE_REQUIRED);
        }

        @Test
        @DisplayName("RN-01: every field is validated through its value object")
        void validatesEachFieldThroughValueObjects() {
            assertRejectedWith(() -> Coupon.create(null, "Black Friday", DISCOUNT, TOMORROW, false, NOW),
                    COUPON_CODE_REQUIRED);
            assertRejectedWith(() -> Coupon.create("AB-12", "Black Friday", DISCOUNT, TOMORROW, false, NOW),
                    COUPON_CODE_INVALID_LENGTH);
            assertRejectedWith(() -> Coupon.create("ABC123", " ", DISCOUNT, TOMORROW, false, NOW),
                    COUPON_DESCRIPTION_REQUIRED);
            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", null, TOMORROW, false, NOW),
                    COUPON_DISCOUNT_VALUE_REQUIRED);
            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", new BigDecimal("0.49"), TOMORROW, false, NOW),
                    COUPON_DISCOUNT_VALUE_BELOW_MINIMUM);
        }

        @Test
        @DisplayName("D-05: validation is fail-fast in the order code, description, discountValue, expirationDate")
        void validatesInFailFastOrder() {
            assertRejectedWith(() -> Coupon.create(null, null, null, null, null, NOW), COUPON_CODE_REQUIRED);
            assertRejectedWith(() -> Coupon.create("ABC123", null, null, null, null, NOW), COUPON_DESCRIPTION_REQUIRED);
            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", null, null, null, NOW),
                    COUPON_DISCOUNT_VALUE_REQUIRED);
            assertRejectedWith(() -> Coupon.create("ABC123", "Black Friday", DISCOUNT, null, null, NOW),
                    COUPON_EXPIRATION_DATE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("restore")
    class Restore {

        @Test
        @DisplayName("RN-10: a coupon expired since its creation can still be restored")
        void restoresExpiredCouponWithoutValidatingAgainstNow() {
            UUID id = UUID.randomUUID();
            Instant longAgo = Instant.parse("2020-01-01T00:00:00Z");

            Coupon coupon = Coupon.restore(id, "ABC123", "Black Friday", DISCOUNT, longAgo, true, false, null, 3L);

            assertThat(coupon.id()).isEqualTo(id);
            assertThat(coupon.code().value()).isEqualTo("ABC123");
            assertThat(coupon.description().value()).isEqualTo("Black Friday");
            assertThat(coupon.discountValue().value()).isEqualTo(DISCOUNT);
            assertThat(coupon.expirationDate()).isEqualTo(longAgo);
            assertThat(coupon.isPublished()).isTrue();
            assertThat(coupon.isRedeemed()).isFalse();
            assertThat(coupon.version()).isEqualTo(3L);
            assertThat(coupon.status()).isEqualTo(CouponStatus.ACTIVE);
        }

        @Test
        @DisplayName("RN-13: a restored deleted coupon has status DELETED")
        void restoresDeletedCoupon() {
            Coupon coupon = Coupon.restore(UUID.randomUUID(), "ABC123", "Black Friday", DISCOUNT, TOMORROW,
                    false, false, NOW, 1L);

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.deletedAt()).contains(NOW);
            assertThat(coupon.status()).isEqualTo(CouponStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("RN-11 / RN-13: soft delete records deletedAt and keeps every other field")
        void softDeletesKeepingData() {
            Coupon coupon = Coupon.create("ABC-123", "Black Friday", DISCOUNT, TOMORROW, true, NOW);
            Instant deletionTime = NOW.plusSeconds(60);

            coupon.delete(deletionTime);

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.deletedAt()).contains(deletionTime);
            assertThat(coupon.status()).isEqualTo(CouponStatus.DELETED);
            assertThat(coupon.code().value()).isEqualTo("ABC123");
            assertThat(coupon.description().value()).isEqualTo("Black Friday");
            assertThat(coupon.discountValue().value()).isEqualTo(DISCOUNT);
            assertThat(coupon.expirationDate()).isEqualTo(TOMORROW);
            assertThat(coupon.isPublished()).isTrue();
            assertThat(coupon.isRedeemed()).isFalse();
        }

        @Test
        @DisplayName("RN-10: an expired coupon can be deleted")
        void deletesExpiredCoupon() {
            Coupon coupon = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, false, NOW);
            Instant afterExpiration = TOMORROW.plus(Duration.ofDays(30));

            coupon.delete(afterExpiration);

            assertThat(coupon.status()).isEqualTo(CouponStatus.DELETED);
        }

        @Test
        @DisplayName("RN-12: a deleted coupon cannot be deleted again")
        void rejectsDeletingTwice() {
            Coupon coupon = Coupon.create("ABC123", "Black Friday", DISCOUNT, TOMORROW, false, NOW);
            coupon.delete(NOW);

            assertThatThrownBy(() -> coupon.delete(NOW.plusSeconds(1)))
                    .isInstanceOfSatisfying(CouponAlreadyDeletedException.class, exception -> {
                        assertThat(exception.couponId()).isEqualTo(coupon.id());
                        assertThat(exception.errorCode()).isEqualTo(CouponErrorCode.COUPON_ALREADY_DELETED);
                        assertThat(exception).hasMessage("Coupon is already deleted.");
                    });
            assertThat(coupon.deletedAt()).contains(NOW);
        }
    }

    private static void assertRejectedWith(ThrowingSupplier<Coupon> creation, CouponErrorCode expected) {
        assertThatThrownBy(creation::get)
                .isInstanceOfSatisfying(InvalidCouponException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(expected));
    }
}

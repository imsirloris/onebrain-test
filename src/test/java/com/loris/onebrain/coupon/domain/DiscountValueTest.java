package com.loris.onebrain.coupon.domain;

import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DISCOUNT_VALUE_BELOW_MINIMUM;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DISCOUNT_VALUE_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DiscountValue")
class DiscountValueTest {

    @Test
    @DisplayName("RN-01: discount value is required")
    void rejectsMissingValue() {
        assertRejectedWith(null, COUPON_DISCOUNT_VALUE_REQUIRED);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"0.49", "0.4999", "0", "0.0", "-0.5", "-1"})
    @DisplayName("RN-06: values below 0.5 are rejected")
    void rejectsValuesBelowMinimum(String value) {
        assertRejectedWith(new BigDecimal(value), COUPON_DISCOUNT_VALUE_BELOW_MINIMUM);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"0.5", "0.50", "0.5000", "5E-1"})
    @DisplayName("RN-06: the minimum itself is accepted regardless of scale")
    void acceptsMinimumWithAnyScale(String value) {
        assertThat(new DiscountValue(new BigDecimal(value)).value()).isEqualByComparingTo("0.5");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"0.51", "0.8", "1", "100", "999999999999999.9999"})
    @DisplayName("RN-06: there is no maximum value")
    void acceptsValuesAboveMinimum(String value) {
        assertThat(new DiscountValue(new BigDecimal(value)).value()).isEqualByComparingTo(value);
    }

    @Test
    @DisplayName("D-11: the received scale is preserved, no rounding")
    void preservesScale() {
        assertThat(new DiscountValue(new BigDecimal("0.555")).value()).isEqualTo(new BigDecimal("0.555"));
    }

    @Test
    void exposesTheMinimum() {
        assertThat(DiscountValue.MINIMUM).isEqualByComparingTo("0.5");
    }

    private static void assertRejectedWith(BigDecimal value, CouponErrorCode expected) {
        assertThatThrownBy(() -> new DiscountValue(value))
                .isInstanceOfSatisfying(InvalidCouponException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(expected));
    }
}

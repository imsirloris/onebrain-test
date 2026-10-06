package com.loris.onebrain.coupon.domain;

import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_DESCRIPTION_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CouponDescription")
class CouponDescriptionTest {

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n "})
    @DisplayName("RN-01 / RN-14: description is required and cannot be blank")
    void rejectsMissingDescription(String raw) {
        assertThatThrownBy(() -> new CouponDescription(raw))
                .isInstanceOfSatisfying(InvalidCouponException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(COUPON_DESCRIPTION_REQUIRED));
    }

    @Test
    @DisplayName("RN-14: leading and trailing whitespace is removed")
    void stripsSurroundingWhitespace() {
        assertThat(new CouponDescription("  Black Friday  ").value()).isEqualTo("Black Friday");
    }

    @Test
    void keepsInnerWhitespace() {
        assertThat(new CouponDescription("Black   Friday").value()).isEqualTo("Black   Friday");
    }

    @Test
    @DisplayName("D-12: there is no maximum length")
    void acceptsVeryLongDescription() {
        String longDescription = "a".repeat(10_000);

        assertThat(new CouponDescription(longDescription).value()).hasSize(10_000);
    }
}

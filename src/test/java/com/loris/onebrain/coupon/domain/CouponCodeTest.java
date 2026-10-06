package com.loris.onebrain.coupon.domain;

import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_CODE_INVALID_LENGTH;
import static com.loris.onebrain.coupon.domain.CouponErrorCode.COUPON_CODE_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CouponCode")
class CouponCodeTest {

    @Nested
    @DisplayName("RN-02 / RN-03: sanitization keeps exactly 6 alphanumeric characters")
    class Sanitization {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "ABC123       | ABC123",
                "123456       | 123456",
                "ABC-123      | ABC123",
                "A@B#C$1%2&3  | ABC123",
                "'  AB C1 23 '| ABC123",
                "AB_C.12/3    | ABC123",
                "(ABC)[123]   | ABC123",
                "AB🎉C123     | ABC123"
        })
        void removesSpecialCharacters(String raw, String expected) {
            assertThat(new CouponCode(raw).value()).isEqualTo(expected);
        }

        @Test
        void keepsAlreadySanitizedCodeUnchanged() {
            CouponCode code = new CouponCode("ABC-123");

            assertThat(new CouponCode(code.value())).isEqualTo(code);
        }

        @Test
        void isComparedByItsSanitizedValue() {
            assertThat(new CouponCode("abc-123")).isEqualTo(new CouponCode("ABC123"));
        }
    }

    @Nested
    @DisplayName("RN-04: accents are normalized, non-decomposable letters are removed")
    class Accents {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "ÁBÇ123  | ABC123",
                "CAFÉ12  | CAFE12",
                "café12  | CAFE12",
                "ñandu1  | NANDU1",
                "ÀÉÎÕÜ1  | AEIOU1"
        })
        void convertsAccentedLettersToTheirBaseLetter(String raw, String expected) {
            assertThat(new CouponCode(raw).value()).isEqualTo(expected);
        }

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "ßABC123 | ABC123",
                "ÆABC123 | ABC123",
                "øABC123 | ABC123"
        })
        void removesLettersWithoutAsciiDecomposition(String raw, String expected) {
            assertThat(new CouponCode(raw).value()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("RN-05: code is uppercased regardless of the JVM locale")
    class Uppercase {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "abc123   | ABC123",
                "aBc-12_3 | ABC123"
        })
        void normalizesToUppercase(String raw, String expected) {
            assertThat(new CouponCode(raw).value()).isEqualTo(expected);
        }

        @Test
        void doesNotDependOnTurkishLocaleDotlessI() {
            Locale original = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));

                assertThat(new CouponCode("i1b2c3").value()).isEqualTo("I1B2C3");
            } finally {
                Locale.setDefault(original);
            }
        }
    }

    @Nested
    @DisplayName("RN-03: rejects codes that do not have exactly 6 alphanumeric characters")
    class InvalidLength {

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {
                "ABC12",
                "AB-12",
                "A",
                "!!!!!!",
                "@#$%&*()",
                "ÆBC12",
                "١٢٣٤٥٦"
        })
        void rejectsFewerThanSixAfterSanitization(String raw) {
            assertRejectedWith(raw, COUPON_CODE_INVALID_LENGTH);
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {
                "ABC1234",
                "ABC-1234",
                "ABCDEFGHIJ"
        })
        void rejectsMoreThanSixAfterSanitization(String raw) {
            assertRejectedWith(raw, COUPON_CODE_INVALID_LENGTH);
        }

        @Test
        void tellsTheClientThatSixAlphanumericCharactersAreRequired() {
            assertThatThrownBy(() -> new CouponCode("AB-12"))
                    .hasMessageContaining("exactly 6 alphanumeric characters");
        }
    }

    @Nested
    @DisplayName("RN-01: code is required")
    class Required {

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void rejectsMissingCode(String raw) {
            assertRejectedWith(raw, COUPON_CODE_REQUIRED);
        }
    }

    private static void assertRejectedWith(String raw, CouponErrorCode expected) {
        assertThatThrownBy(() -> new CouponCode(raw))
                .isInstanceOfSatisfying(InvalidCouponException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(expected));
    }
}

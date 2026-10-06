package com.loris.onebrain.coupon.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public record CouponCode(String value) {

    public static final int LENGTH = 6;

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{M}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

    public CouponCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_CODE_REQUIRED);
        }
        value = sanitize(value);
        if (value.length() != LENGTH) {
            throw new InvalidCouponException(CouponErrorCode.COUPON_CODE_INVALID_LENGTH);
        }
    }

    private static String sanitize(String raw) {
        String decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD);
        String withoutMarks = DIACRITICAL_MARKS.matcher(decomposed).replaceAll("");
        String alphanumeric = NON_ALPHANUMERIC.matcher(withoutMarks).replaceAll("");
        return alphanumeric.toUpperCase(Locale.ROOT);
    }
}

package com.loris.onebrain.coupon.domain;

public enum CouponErrorCode {

    COUPON_CODE_REQUIRED("Coupon code is required."),
    COUPON_CODE_INVALID_LENGTH("Coupon code must contain exactly 6 alphanumeric characters after removing special characters."),
    COUPON_DESCRIPTION_REQUIRED("Coupon description is required."),
    COUPON_DISCOUNT_VALUE_REQUIRED("Discount value is required."),
    COUPON_DISCOUNT_VALUE_BELOW_MINIMUM("Discount value must be at least 0.5."),
    COUPON_EXPIRATION_DATE_REQUIRED("Expiration date is required."),
    COUPON_EXPIRATION_DATE_IN_PAST("Expiration date cannot be in the past."),
    COUPON_ALREADY_DELETED("Coupon is already deleted.");

    private final String message;

    CouponErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}

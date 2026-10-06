package com.loris.onebrain.coupon.domain;

public class InvalidCouponException extends DomainException {

    public InvalidCouponException(CouponErrorCode errorCode) {
        super(errorCode);
    }
}

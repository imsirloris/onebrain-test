package com.loris.onebrain.coupon.domain;

public abstract class DomainException extends RuntimeException {

    private final CouponErrorCode errorCode;

    protected DomainException(CouponErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public CouponErrorCode errorCode() {
        return errorCode;
    }
}

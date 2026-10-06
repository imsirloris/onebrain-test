package com.loris.onebrain.coupon.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.loris.onebrain.coupon.application.CouponConcurrentModificationException;
import com.loris.onebrain.coupon.application.CouponNotFoundException;
import com.loris.onebrain.coupon.domain.CouponAlreadyDeletedException;
import com.loris.onebrain.coupon.domain.InvalidCouponException;

@RestControllerAdvice
public class ApiExceptionHandler {

    static final String CODE_PROPERTY = "code";

    @ExceptionHandler(InvalidCouponException.class)
    public ProblemDetail handleInvalidCoupon(InvalidCouponException exception) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, exception.errorCode().name(), exception.getMessage());
    }

    @ExceptionHandler(CouponAlreadyDeletedException.class)
    public ProblemDetail handleAlreadyDeleted(CouponAlreadyDeletedException exception) {
        return problem(HttpStatus.CONFLICT, exception.errorCode().name(), exception.getMessage());
    }

    @ExceptionHandler(CouponConcurrentModificationException.class)
    public ProblemDetail handleConcurrentModification(CouponConcurrentModificationException exception) {
        return problem(HttpStatus.CONFLICT, "COUPON_CONCURRENT_MODIFICATION", exception.getMessage());
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ProblemDetail handleNotFound(CouponNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedRequest(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is malformed or has invalid field types.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Path parameter has an invalid format.");
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE_PROPERTY, code);
        return problem;
    }
}

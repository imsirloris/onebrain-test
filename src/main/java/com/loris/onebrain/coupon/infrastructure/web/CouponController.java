package com.loris.onebrain.coupon.infrastructure.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.loris.onebrain.coupon.application.CouponResult;
import com.loris.onebrain.coupon.application.CreateCouponUseCase;
import com.loris.onebrain.coupon.application.DeleteCouponUseCase;
import com.loris.onebrain.coupon.application.GetCouponUseCase;

@RestController
public class CouponController implements CouponApi {

    private final CreateCouponUseCase createCouponUseCase;
    private final GetCouponUseCase getCouponUseCase;
    private final DeleteCouponUseCase deleteCouponUseCase;

    @Autowired
    public CouponController(CreateCouponUseCase createCouponUseCase,
                            GetCouponUseCase getCouponUseCase,
                            DeleteCouponUseCase deleteCouponUseCase) {
        this.createCouponUseCase = createCouponUseCase;
        this.getCouponUseCase = getCouponUseCase;
        this.deleteCouponUseCase = deleteCouponUseCase;
    }

    @Override
    public ResponseEntity<CouponResponse> create(CreateCouponRequest request) {
        CouponResult result = createCouponUseCase.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();
        return ResponseEntity.created(location).body(CouponResponse.from(result));
    }

    @Override
    public ResponseEntity<CouponResponse> get(UUID id) {
        return ResponseEntity.ok(CouponResponse.from(getCouponUseCase.execute(id)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        deleteCouponUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

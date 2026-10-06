package com.loris.onebrain.coupon.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.loris.onebrain.coupon.application.CreateCouponUseCase;
import com.loris.onebrain.coupon.application.DeleteCouponUseCase;
import com.loris.onebrain.coupon.application.GetCouponUseCase;
import com.loris.onebrain.coupon.application.port.CouponRepository;

@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CreateCouponUseCase createCouponUseCase(CouponRepository repository, Clock clock) {
        return new CreateCouponUseCase(repository, clock);
    }

    @Bean
    public GetCouponUseCase getCouponUseCase(CouponRepository repository) {
        return new GetCouponUseCase(repository);
    }

    @Bean
    public DeleteCouponUseCase deleteCouponUseCase(CouponRepository repository, Clock clock) {
        return new DeleteCouponUseCase(repository, clock);
    }
}

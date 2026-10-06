package com.loris.onebrain.coupon.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI couponOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Coupon API")
                .description("Create, query and soft delete discount coupons.")
                .version("v1"));
    }
}

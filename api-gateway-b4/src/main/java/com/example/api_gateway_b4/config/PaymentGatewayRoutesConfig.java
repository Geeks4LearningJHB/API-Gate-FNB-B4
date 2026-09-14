package com.example.api_gateway_b4.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Route definitions for the Payment Microservice.
 * Routes client traffic for single payments, recurring payments, batch CSV processing,
 * and admin actions (audit logs, manual review) to the Payment API Service.
 */
@Configuration
public class PaymentGatewayRoutesConfig {

    @Value("${payment.service.url:${PAYMENT_SERVICE_URL:http://localhost:8081}}")
    private String paymentServiceUrl;

    @Bean
    public RouteLocator paymentRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("payment-service", r -> r
                        .path(
                                "/payments/**",
                                "/payments",
                                "/payment/**",
                                "/payment",
                                "/batch/**",
                                "/batch",
                                "/admin/**"
                        )
                        .uri(paymentServiceUrl))
                .build();
    }
}

package com.example.api_gateway_b4.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Route definitions for the Banking Microservice.
 * All banking endpoints are served under /api (accounts, balance, cards,
 * transactions, etc.), so a single predicate forwards that whole prefix to the
 * Banking API Service. The Authorization (JWT) header is forwarded automatically.
 */
@Configuration
public class BankingGatewayRoutesConfig {

    @Value("${banking.service.url:${BANKING_SERVICE_URL:https://fnb-b4-ibs-banking-api-service.onrender.com}}")
    private String bankingServiceUrl;

    @Bean
    public RouteLocator bankingRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("banking-service", r -> r
                        .path(
                                "/api/**",
                                "/api"
                        )
                        .uri(bankingServiceUrl))
                .build();
    }
}

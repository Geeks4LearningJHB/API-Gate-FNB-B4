package com.example.api_gateway_b4.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Route definitions for the Transaction Microservice.
 * Routes client traffic for transactions to the Transaction API Service.
 */
@Configuration
@Order(-1)
public class TransactionGatewayRoutesConfig {

    @Value("${transaction.service.url:${TRANSACTION_SERVICE_URL:http://localhost:8083}}")
    private String transactionServiceUrl;

    @Bean
    public RouteLocator transactionRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("transaction-service", r -> r
                        .path(
                                "/api/transactions/customer/**",
                                "/api/audit-logs/**",
                                "/api/audit-logs",
                                "/api/test-kafka/**",
                                "/api/test-kafka",
                                "/camunda/**",
                                "/camunda"
                        )
                        .uri(transactionServiceUrl))
                .build();
    }
}

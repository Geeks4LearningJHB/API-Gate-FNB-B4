package com.example.api_gateway_b4.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Route definitions for the User Management Microservice.
 * Routes client traffic for authentication (login, register, password reset)
 * and customer profile management to the User Service.
 */
@Configuration
public class UserManagementGatewayRoutesConfig {

    @Value("${user.service.url:${USER_SERVICE_URL:http://localhost:8092}}")
    private String userServiceUrl;

    @Bean
    public RouteLocator userManagementRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-auth-service", r -> r
                        .path(
                                "/api/auth/**",
                                "/api/auth"
                        )
                        .uri(userServiceUrl))
                .route("user-customer-service", r -> r
                        .path(
                                "/api/customer/**",
                                "/api/customer"
                        )
                        .uri(userServiceUrl))
                .build();
    }
}

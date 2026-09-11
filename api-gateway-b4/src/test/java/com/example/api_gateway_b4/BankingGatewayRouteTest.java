package com.example.api_gateway_b4;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BankingGatewayRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Verify banking-service route is registered with correct URI")
    void testBankingRouteRegistered() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route bankingRoute = routes.stream()
                .filter(r -> "banking-service".equals(r.getId()))
                .findFirst()
                .orElse(null);

        assertThat(bankingRoute).isNotNull();
        // Gateway normalizes an https URI to its implicit port (:443).
        assertThat(bankingRoute.getUri()).isEqualTo(URI.create("https://fnb-b4-ibs-banking-api-service.onrender.com:443"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api",
            "/api/health",
            "/api/accounts",
            "/api/create-account",
            "/api/accounts/by-account-number/123",
            "/api/accounts/by-customer/456",
            "/api/balance/123",
            "/api/cards",
            "/api/cards/789",
            "/api/transactions",
            "/api/transactions/history",
            "/api/transactions/balance-check",
            "/api/transactions/by-customer/456"
    })
    @DisplayName("Verify that Banking Microservice paths match the route predicate")
    void testBankingPathsMatchRoute(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route bankingRoute = routes.stream()
                .filter(r -> "banking-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("banking-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(bankingRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should match banking-service route predicate", path)
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/payments",
            "/payment/test",
            "/batch/123",
            "/admin/audit-logs",
            "/users",
            "/status/xyz"
    })
    @DisplayName("Verify non-banking paths do not match banking-service route predicate")
    void testNonBankingPathsDoNotMatch(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route bankingRoute = routes.stream()
                .filter(r -> "banking-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("banking-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(bankingRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should NOT match banking-service route predicate", path)
                .isFalse();
    }
}

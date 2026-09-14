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
class TransactionGatewayRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Verify transaction-service route is registered with correct URI")
    void testTransactionRouteRegistered() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route transactionRoute = routes.stream()
                .filter(r -> "transaction-service".equals(r.getId()))
                .findFirst()
                .orElse(null);

        assertThat(transactionRoute).isNotNull();
        assertThat(transactionRoute.getUri()).isEqualTo(URI.create("http://localhost:8083"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/transactions/customer/123/history-requests",
            "/api/transactions/customer/456/recent",
            "/api/audit-logs",
            "/api/audit-logs/123",
            "/api/test-kafka/simulate-payment",
            "/camunda/app/cockpit/default/"
    })
    @DisplayName("Verify that Transaction Microservice paths match the route predicate")
    void testTransactionPathsMatchRoute(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route transactionRoute = routes.stream()
                .filter(r -> "transaction-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("transaction-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(transactionRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should match transaction-service route predicate", path)
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/payments",
            "/payments/create",
            "/api/auth",
            "/api/customer",
            "/api/transactions",
            "/api/transactions/history"
    })
    @DisplayName("Verify non-transaction paths do not match transaction-service route predicate")
    void testNonTransactionPathsDoNotMatch(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route transactionRoute = routes.stream()
                .filter(r -> "transaction-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("transaction-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(transactionRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should NOT match transaction-service route predicate", path)
                .isFalse();
    }
}

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
class PaymentGatewayRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Verify payment-service route is registered with correct URI")
    void testPaymentRouteRegistered() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route paymentRoute = routes.stream()
                .filter(r -> "payment-service".equals(r.getId()))
                .findFirst()
                .orElse(null);

        assertThat(paymentRoute).isNotNull();
        assertThat(paymentRoute.getUri()).isEqualTo(URI.create("http://localhost:8081"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/payments",
            "/payments/create",
            "/payments/00000000-0000-0000-0000-000000000000",
            "/payments/123/status",
            "/payments/123/proof",
            "/payments/recurring",
            "/payments/recurring/456",
            "/payment",
            "/payment/test",
            "/batch",
            "/batch/123",
            "/admin/audit-logs",
            "/admin/audit-logs/789",
            "/admin/manual-review",
            "/admin/manual-review/789/decision"
    })
    @DisplayName("Verify that Payment Microservice paths match the route predicate")
    void testPaymentPathsMatchRoute(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route paymentRoute = routes.stream()
                .filter(r -> "payment-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("payment-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(paymentRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should match payment-service route predicate", path)
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/users",
            "/users/profile",
            "/balance/123",
            "/transactions",
            "/status/xyz"
    })
    @DisplayName("Verify non-payment paths do not match payment-service route predicate")
    void testNonPaymentPathsDoNotMatch(String path) {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Route paymentRoute = routes.stream()
                .filter(r -> "payment-service".equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("payment-service route not found"));

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Boolean matches = Mono.from(paymentRoute.getPredicate().apply(exchange)).block();
        assertThat(matches)
                .withFailMessage("Path %s should NOT match payment-service route predicate", path)
                .isFalse();
    }
}

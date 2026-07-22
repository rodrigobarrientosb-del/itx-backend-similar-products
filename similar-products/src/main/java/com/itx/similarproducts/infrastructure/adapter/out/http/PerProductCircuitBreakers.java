package com.itx.similarproducts.infrastructure.adapter.out.http;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * Un circuit breaker por productId: si solo peta el producto 5, el resto sigue llamando al upstream.
 */
final class PerProductCircuitBreakers {

    static final String NAME_PREFIX = "productApi-";

    private PerProductCircuitBreakers() {
    }

    static CircuitBreaker of(CircuitBreakerRegistry registry, String productId) {
        return registry.circuitBreaker(NAME_PREFIX + productId);
    }
}

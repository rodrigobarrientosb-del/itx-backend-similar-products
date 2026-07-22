package com.itx.similarproducts.infrastructure.adapter.out.http;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class PerProductCircuitBreakersTest {

    @Test
    void returnsDistinctBreakersPerProductIdAndReusesSameInstance() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.ofDefaults());

        CircuitBreaker first5 = PerProductCircuitBreakers.of(registry, "5");
        CircuitBreaker second5 = PerProductCircuitBreakers.of(registry, "5");
        CircuitBreaker product2 = PerProductCircuitBreakers.of(registry, "2");

        assertSame(first5, second5);
        assertNotSame(first5, product2);
    }
}

package com.itx.similarproducts.infrastructure.adapter.out.http;

import com.itx.similarproducts.domain.exception.ProductNotFoundException;
import com.itx.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebClientProductDetailAdapterCircuitBreakerTest {

    private MockWebServer server;
    private CircuitBreakerRegistry registry;
    private WebClientProductDetailAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(1)
                .ignoreExceptions(ProductNotFoundException.class)
                .build();
        registry = CircuitBreakerRegistry.of(config);

        UpstreamProperties props = new UpstreamProperties();
        props.setBaseUrl(server.url("/").toString().replaceAll("/$", ""));
        props.setTimeoutMs(500);

        adapter = new WebClientProductDetailAdapter(WebClient.builder(), props, registry);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void openCircuitOnProduct5DoesNotBlockProduct2() {
        for (int i = 0; i < 4; i++) {
            server.enqueue(new MockResponse().setResponseCode(500));
            StepVerifier.create(adapter.findById("5")).expectError().verify();
        }

        CircuitBreaker cb5 = PerProductCircuitBreakers.of(registry, "5");
        assertEquals(CircuitBreaker.State.OPEN, cb5.getState());

        int requestsAfterProduct5Failures = server.getRequestCount();

        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"id":"2","name":"Dress","price":19.99,"availability":true}
                        """));

        StepVerifier.create(adapter.findById("2"))
                .expectNextMatches(p -> p.getId().equals("2") && p.getName().equals("Dress"))
                .verifyComplete();

        assertEquals(CircuitBreaker.State.CLOSED, PerProductCircuitBreakers.of(registry, "2").getState());
        assertEquals(requestsAfterProduct5Failures + 1, server.getRequestCount());

        StepVerifier.create(adapter.findById("5"))
                .expectError(CallNotPermittedException.class)
                .verify();

        // Circuito abierto: no vuelve a pegarle al upstream por el producto 5.
        assertEquals(requestsAfterProduct5Failures + 1, server.getRequestCount());
    }

    @Test
    void notFoundDoesNotOpenCircuitForThatProduct() {
        for (int i = 0; i < 4; i++) {
            server.enqueue(new MockResponse().setResponseCode(404));
            StepVerifier.create(adapter.findById("5"))
                    .expectError(ProductNotFoundException.class)
                    .verify();
        }

        assertEquals(CircuitBreaker.State.CLOSED, PerProductCircuitBreakers.of(registry, "5").getState());
        assertEquals(4, server.getRequestCount());
    }
}

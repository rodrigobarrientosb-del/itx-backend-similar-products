package com.itx.similarproducts.infrastructure.adapter.out.http;

import com.itx.similarproducts.domain.exception.ProductNotFoundException;
import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class WebClientProductDetailAdapter implements ProductDetailPort {

    private final WebClient webClient;
    private final Duration timeout;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public WebClientProductDetailAdapter(
            WebClient.Builder webClientBuilder,
            UpstreamProperties properties,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.timeout = Duration.ofMillis(properties.getTimeoutMs());
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Mono<Product> findById(String productId) {
        CircuitBreaker circuitBreaker = PerProductCircuitBreakers.of(circuitBreakerRegistry, productId);
        return webClient.get()
                .uri("/product/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.createException().flatMap(ex -> {
                            if (ex.getStatusCode().value() == 404) {
                                return Mono.error(new ProductNotFoundException(productId));
                            }
                            return Mono.error(ex);
                        }))
                .bodyToMono(UpstreamProductDto.class)
                .timeout(timeout)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new ProductNotFoundException(productId))
                .map(this::toDomain);
    }

    private Product toDomain(UpstreamProductDto dto) {
        if (dto == null || dto.id() == null || dto.name() == null
                || dto.price() == null || dto.availability() == null) {
            throw new IllegalStateException("Invalid product payload from upstream");
        }
        return new Product(dto.id(), dto.name(), dto.price(), dto.availability());
    }
}

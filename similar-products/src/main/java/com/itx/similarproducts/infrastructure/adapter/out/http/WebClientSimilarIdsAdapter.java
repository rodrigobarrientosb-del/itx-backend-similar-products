package com.itx.similarproducts.infrastructure.adapter.out.http;

import com.itx.similarproducts.domain.exception.ProductNotFoundException;
import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import com.itx.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class WebClientSimilarIdsAdapter implements SimilarIdsPort {

    private final WebClient webClient;
    private final Duration timeout;
    private final CircuitBreaker circuitBreaker;

    public WebClientSimilarIdsAdapter(
            WebClient.Builder webClientBuilder,
            UpstreamProperties properties,
            CircuitBreaker productApiCircuitBreaker
    ) {
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.timeout = Duration.ofMillis(properties.getTimeoutMs());
        this.circuitBreaker = productApiCircuitBreaker;
    }

    @Override
    public Flux<String> findSimilarIds(String productId) {
        return webClient.get()
                .uri("/product/{productId}/similarids", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.createException().flatMap(ex -> {
                            if (ex.getStatusCode().value() == 404) {
                                return Mono.error(new ProductNotFoundException(productId));
                            }
                            return Mono.error(ex);
                        }))
                .bodyToMono(new ParameterizedTypeReference<List<Object>>() {})
                .timeout(timeout)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new ProductNotFoundException(productId))
                .flatMapMany(ids -> Flux.fromIterable(ids)
                        .map(String::valueOf)
                        .filter(id -> !id.isBlank()));
    }
}

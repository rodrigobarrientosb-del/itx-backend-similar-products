package com.itx.similarproducts.infrastructure.config;

import com.itx.similarproducts.domain.exception.ProductNotFoundException;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import com.itx.similarproducts.infrastructure.adapter.out.cache.CachingProductDetailAdapter;
import com.itx.similarproducts.infrastructure.adapter.out.cache.CachingSimilarIdsAdapter;
import com.itx.similarproducts.infrastructure.adapter.out.http.WebClientProductDetailAdapter;
import com.itx.similarproducts.infrastructure.adapter.out.http.WebClientSimilarIdsAdapter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({UpstreamProperties.class, CacheProperties.class})
public class ApplicationConfig {

    @Bean
    WebClient.Builder webClientBuilder() {
        ConnectionProvider provider = ConnectionProvider.builder("upstream")
                .maxConnections(500)
                .pendingAcquireMaxCount(2_000)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(5))
                .compress(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(80)
                .slowCallDurationThreshold(Duration.ofMillis(1500))
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .slidingWindowSize(40)
                .minimumNumberOfCalls(20)
                .permittedNumberOfCallsInHalfOpenState(10)
                .ignoreExceptions(ProductNotFoundException.class)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    CircuitBreaker productApiCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("productApi");
    }

    @Bean
    SimilarIdsPort similarIdsPort(
            WebClient.Builder webClientBuilder,
            UpstreamProperties upstreamProperties,
            CircuitBreaker productApiCircuitBreaker,
            CacheProperties cacheProperties
    ) {
        SimilarIdsPort http = new WebClientSimilarIdsAdapter(
                webClientBuilder, upstreamProperties, productApiCircuitBreaker);
        return new CachingSimilarIdsAdapter(http, cacheProperties);
    }

    @Bean
    ProductDetailPort productDetailPort(
            WebClient.Builder webClientBuilder,
            UpstreamProperties upstreamProperties,
            CircuitBreaker productApiCircuitBreaker,
            CacheProperties cacheProperties
    ) {
        ProductDetailPort http = new WebClientProductDetailAdapter(
                webClientBuilder, upstreamProperties, productApiCircuitBreaker);
        return new CachingProductDetailAdapter(http, cacheProperties);
    }
}

package com.itx.similarproducts.application.service;

import com.itx.similarproducts.domain.exception.ProductNotFoundException;
import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSimilarProductsServiceTest {

    @Mock
    private SimilarIdsPort similarIdsPort;

    @Mock
    private ProductDetailPort productDetailPort;

    private GetSimilarProductsService service;

    @BeforeEach
    void setUp() {
        service = new GetSimilarProductsService(similarIdsPort, productDetailPort);
    }

    @Test
    void returnsProductsInSimilarityOrder() {
        when(similarIdsPort.findSimilarIds("1")).thenReturn(Flux.just("2", "3"));
        when(productDetailPort.findById("2"))
                .thenReturn(Mono.just(new Product("2", "Dress", 19.99, true)));
        when(productDetailPort.findById("3"))
                .thenReturn(Mono.just(new Product("3", "Blazer", 29.99, false)));

        StepVerifier.create(service.getSimilarProducts("1"))
                .expectNextMatches(p -> p.getId().equals("2") && p.getName().equals("Dress"))
                .expectNextMatches(p -> p.getId().equals("3") && !p.isAvailability())
                .verifyComplete();
    }

    @Test
    void skipsUnavailableSimilarProductsAndKeepsSuccessfulOnes() {
        when(similarIdsPort.findSimilarIds("5")).thenReturn(Flux.just("1", "2", "6"));
        when(productDetailPort.findById("1"))
                .thenReturn(Mono.just(new Product("1", "Shirt", 9.99, true)));
        when(productDetailPort.findById("2"))
                .thenReturn(Mono.just(new Product("2", "Dress", 19.99, true)));
        when(productDetailPort.findById("6"))
                .thenReturn(Mono.error(new RuntimeException("upstream 500")));

        StepVerifier.create(service.getSimilarProducts("5"))
                .expectNextMatches(p -> p.getId().equals("1"))
                .expectNextMatches(p -> p.getId().equals("2"))
                .verifyComplete();
    }

    @Test
    void skipsNotFoundSimilarProductDetails() {
        when(similarIdsPort.findSimilarIds("4")).thenReturn(Flux.just("1", "2", "5"));
        when(productDetailPort.findById("1"))
                .thenReturn(Mono.just(new Product("1", "Shirt", 9.99, true)));
        when(productDetailPort.findById("2"))
                .thenReturn(Mono.just(new Product("2", "Dress", 19.99, true)));
        when(productDetailPort.findById("5"))
                .thenReturn(Mono.error(new ProductNotFoundException("5")));

        StepVerifier.create(service.getSimilarProducts("4"))
                .expectNextMatches(p -> p.getId().equals("1"))
                .expectNextMatches(p -> p.getId().equals("2"))
                .verifyComplete();
    }

    @Test
    void propagatesNotFoundWhenSimilarIdsAreMissing() {
        when(similarIdsPort.findSimilarIds("99"))
                .thenReturn(Flux.error(new ProductNotFoundException("99")));

        StepVerifier.create(service.getSimilarProducts("99"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void returnsEmptyWhenAllSimilarDetailsFail() {
        when(similarIdsPort.findSimilarIds("x")).thenReturn(Flux.just("a", "b"));
        when(productDetailPort.findById(anyString()))
                .thenReturn(Mono.error(new RuntimeException("down")));

        StepVerifier.create(service.getSimilarProducts("x"))
                .verifyComplete();
    }
}

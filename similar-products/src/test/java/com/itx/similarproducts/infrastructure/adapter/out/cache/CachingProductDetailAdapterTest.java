package com.itx.similarproducts.infrastructure.adapter.out.cache;

import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.infrastructure.config.CacheProperties;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

class CachingProductDetailAdapterTest {

    @Test
    void cachesSuccessfulLookups() {
        AtomicInteger calls = new AtomicInteger();
        ProductDetailPort delegate = id -> {
            calls.incrementAndGet();
            return Mono.just(new Product(id, "Shirt", 9.99, true));
        };

        CacheProperties props = new CacheProperties();
        props.setProductDetailTtlSeconds(60);
        props.setMaximumSize(100);

        CachingProductDetailAdapter adapter = new CachingProductDetailAdapter(delegate, props);

        StepVerifier.create(adapter.findById("1")).expectNextCount(1).verifyComplete();
        StepVerifier.create(adapter.findById("1")).expectNextCount(1).verifyComplete();

        org.junit.jupiter.api.Assertions.assertEquals(1, calls.get());
    }

    @Test
    void doesNotCacheFailures() {
        AtomicInteger calls = new AtomicInteger();
        ProductDetailPort delegate = id -> {
            calls.incrementAndGet();
            return Mono.error(new RuntimeException("boom"));
        };

        CacheProperties props = new CacheProperties();
        CachingProductDetailAdapter adapter = new CachingProductDetailAdapter(delegate, props);

        StepVerifier.create(adapter.findById("1")).expectError(RuntimeException.class).verify();
        StepVerifier.create(adapter.findById("1")).expectError(RuntimeException.class).verify();

        org.junit.jupiter.api.Assertions.assertEquals(2, calls.get());
    }
}

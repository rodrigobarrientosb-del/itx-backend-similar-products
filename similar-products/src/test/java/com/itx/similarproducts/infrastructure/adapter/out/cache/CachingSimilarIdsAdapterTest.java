package com.itx.similarproducts.infrastructure.adapter.out.cache;

import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import com.itx.similarproducts.infrastructure.config.CacheProperties;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CachingSimilarIdsAdapterTest {

    @Test
    void cachesSuccessfulLookups() {
        AtomicInteger calls = new AtomicInteger();
        SimilarIdsPort delegate = id -> {
            calls.incrementAndGet();
            return Flux.just("2", "3");
        };

        CacheProperties props = new CacheProperties();
        props.setSimilarIdsTtlSeconds(60);
        props.setMaximumSize(100);

        CachingSimilarIdsAdapter adapter = new CachingSimilarIdsAdapter(delegate, props);

        StepVerifier.create(adapter.findSimilarIds("1")).expectNext("2", "3").verifyComplete();
        StepVerifier.create(adapter.findSimilarIds("1")).expectNext("2", "3").verifyComplete();

        assertEquals(1, calls.get());
    }

    @Test
    void doesNotCacheFailures() {
        AtomicInteger calls = new AtomicInteger();
        SimilarIdsPort delegate = id -> {
            calls.incrementAndGet();
            return Flux.error(new RuntimeException("boom"));
        };

        CachingSimilarIdsAdapter adapter = new CachingSimilarIdsAdapter(delegate, new CacheProperties());

        StepVerifier.create(adapter.findSimilarIds("1")).expectError(RuntimeException.class).verify();
        StepVerifier.create(adapter.findSimilarIds("1")).expectError(RuntimeException.class).verify();

        assertEquals(2, calls.get());
    }
}

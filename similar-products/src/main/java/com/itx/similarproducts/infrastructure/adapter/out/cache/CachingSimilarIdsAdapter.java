package com.itx.similarproducts.infrastructure.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import com.itx.similarproducts.infrastructure.config.CacheProperties;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/** Caché delante del puerto de ids similares. */
public class CachingSimilarIdsAdapter implements SimilarIdsPort {

    private final SimilarIdsPort delegate;
    private final Cache<String, List<String>> cache;

    public CachingSimilarIdsAdapter(SimilarIdsPort delegate, CacheProperties cacheProperties) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(cacheProperties.getSimilarIdsTtlSeconds()))
                .maximumSize(cacheProperties.getMaximumSize())
                .build();
    }

    @Override
    public Flux<String> findSimilarIds(String productId) {
        List<String> cached = cache.getIfPresent(productId);
        if (cached != null) {
            return Flux.fromIterable(cached);
        }

        return delegate.findSimilarIds(productId)
                .collectList()
                .doOnNext(ids -> cache.put(productId, List.copyOf(ids)))
                .flatMapMany(Flux::fromIterable);
    }
}

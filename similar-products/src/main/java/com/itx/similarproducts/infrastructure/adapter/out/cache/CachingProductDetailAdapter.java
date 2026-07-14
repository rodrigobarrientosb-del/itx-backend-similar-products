package com.itx.similarproducts.infrastructure.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.infrastructure.config.CacheProperties;
import reactor.core.publisher.Mono;

import java.time.Duration;

/** Caché delante del puerto de fichas de producto. Solo guarda respuestas OK. */
public class CachingProductDetailAdapter implements ProductDetailPort {

    private final ProductDetailPort delegate;
    private final Cache<String, Product> cache;

    public CachingProductDetailAdapter(ProductDetailPort delegate, CacheProperties cacheProperties) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(cacheProperties.getProductDetailTtlSeconds()))
                .maximumSize(cacheProperties.getMaximumSize())
                .build();
    }

    @Override
    public Mono<Product> findById(String productId) {
        Product cached = cache.getIfPresent(productId);
        if (cached != null) {
            return Mono.just(cached);
        }

        return delegate.findById(productId)
                .doOnNext(product -> cache.put(productId, product));
    }
}

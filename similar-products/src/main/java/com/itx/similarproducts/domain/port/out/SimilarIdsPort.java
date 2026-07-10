package com.itx.similarproducts.domain.port.out;

import reactor.core.publisher.Flux;

/** Puerto de salida: ids de productos similares (ya ordenados). */
public interface SimilarIdsPort {

    Flux<String> findSimilarIds(String productId);
}

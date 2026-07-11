package com.itx.similarproducts.application.service;

import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.in.GetSimilarProductsUseCase;
import com.itx.similarproducts.domain.port.out.ProductDetailPort;
import com.itx.similarproducts.domain.port.out.SimilarIdsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Junta ids similares + fichas.
 * Mantiene el orden y, si un detalle falla, lo omite en vez de romper toda la respuesta.
 */
@Service
public class GetSimilarProductsService implements GetSimilarProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSimilarProductsService.class);
    private static final int DETAIL_CONCURRENCY = 8;

    private final SimilarIdsPort similarIdsPort;
    private final ProductDetailPort productDetailPort;

    public GetSimilarProductsService(SimilarIdsPort similarIdsPort, ProductDetailPort productDetailPort) {
        this.similarIdsPort = similarIdsPort;
        this.productDetailPort = productDetailPort;
    }

    @Override
    public Flux<Product> getSimilarProducts(String productId) {
        return similarIdsPort.findSimilarIds(productId)
                .flatMapSequential(
                        similarId -> productDetailPort.findById(similarId)
                                .onErrorResume(error -> {
                                    log.warn("Me salto el similar {}: {}", similarId, error.toString());
                                    return Mono.empty();
                                }),
                        DETAIL_CONCURRENCY
                )
                .distinct(Product::getId);
    }
}

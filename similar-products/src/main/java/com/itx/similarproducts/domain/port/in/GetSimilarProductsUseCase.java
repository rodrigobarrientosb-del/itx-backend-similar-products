package com.itx.similarproducts.domain.port.in;

import com.itx.similarproducts.domain.model.Product;
import reactor.core.publisher.Flux;

/** Caso de uso: devolver los productos similares a uno dado. */
public interface GetSimilarProductsUseCase {

    Flux<Product> getSimilarProducts(String productId);
}

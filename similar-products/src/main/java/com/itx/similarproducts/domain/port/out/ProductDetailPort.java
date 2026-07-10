package com.itx.similarproducts.domain.port.out;

import com.itx.similarproducts.domain.model.Product;
import reactor.core.publisher.Mono;

/** Puerto de salida: ficha de un producto. */
public interface ProductDetailPort {

    Mono<Product> findById(String productId);
}

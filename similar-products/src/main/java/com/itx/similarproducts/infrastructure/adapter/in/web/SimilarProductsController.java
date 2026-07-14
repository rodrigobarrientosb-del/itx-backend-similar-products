package com.itx.similarproducts.infrastructure.adapter.in.web;

import com.itx.similarproducts.domain.model.Product;
import com.itx.similarproducts.domain.port.in.GetSimilarProductsUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/product")
public class SimilarProductsController {

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProductsUseCase) {
        this.getSimilarProductsUseCase = getSimilarProductsUseCase;
    }

    @GetMapping(value = "/{productId}/similar", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductDetailResponse> getSimilarProducts(@PathVariable String productId) {
        return getSimilarProductsUseCase.getSimilarProducts(productId)
                .map(this::toResponse);
    }

    private ProductDetailResponse toResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.isAvailability()
        );
    }
}

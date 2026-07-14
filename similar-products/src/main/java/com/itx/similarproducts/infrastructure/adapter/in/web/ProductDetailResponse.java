package com.itx.similarproducts.infrastructure.adapter.in.web;

public record ProductDetailResponse(
        String id,
        String name,
        double price,
        boolean availability
) {
}

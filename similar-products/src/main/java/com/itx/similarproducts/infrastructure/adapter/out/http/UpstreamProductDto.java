package com.itx.similarproducts.infrastructure.adapter.out.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpstreamProductDto(
        String id,
        String name,
        Double price,
        Boolean availability
) {
}

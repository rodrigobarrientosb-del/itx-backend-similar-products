package com.itx.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private long similarIdsTtlSeconds = 60;
    private long productDetailTtlSeconds = 60;
    private long maximumSize = 10_000;

    public long getSimilarIdsTtlSeconds() {
        return similarIdsTtlSeconds;
    }

    public void setSimilarIdsTtlSeconds(long similarIdsTtlSeconds) {
        this.similarIdsTtlSeconds = similarIdsTtlSeconds;
    }

    public long getProductDetailTtlSeconds() {
        return productDetailTtlSeconds;
    }

    public void setProductDetailTtlSeconds(long productDetailTtlSeconds) {
        this.productDetailTtlSeconds = productDetailTtlSeconds;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }
}

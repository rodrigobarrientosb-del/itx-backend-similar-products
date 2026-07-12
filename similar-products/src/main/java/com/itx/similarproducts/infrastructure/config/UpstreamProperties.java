package com.itx.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upstream")
public class UpstreamProperties {

    /** URL de los mocks / APIs existentes. */
    private String baseUrl = "http://localhost:3001";

    /** Timeout por llamada al upstream, en ms. */
    private long timeoutMs = 2000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}

package com.itx.similarproducts.infrastructure.adapter.in.web;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimilarProductsIntegrationTest {

    private static MockWebServer upstream;
    private static final Map<String, Function<RecordedRequest, MockResponse>> routes = new ConcurrentHashMap<>();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void upstreamProperties(DynamicPropertyRegistry registry) throws IOException {
        upstream = new MockWebServer();
        upstream.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path == null) {
                    return new MockResponse().setResponseCode(404);
                }
                Function<RecordedRequest, MockResponse> handler = routes.get(path);
                if (handler != null) {
                    return handler.apply(request);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        upstream.start();
        registry.add("app.upstream.base-url", () -> upstream.url("/").toString().replaceAll("/$", ""));
        registry.add("app.upstream.timeout-ms", () -> "1000");
        registry.add("app.cache.similar-ids-ttl-seconds", () -> "60");
        registry.add("app.cache.product-detail-ttl-seconds", () -> "60");
    }

    @AfterAll
    void stopUpstream() throws IOException {
        if (upstream != null) {
            upstream.shutdown();
        }
    }

    @Test
    void returnsSimilarProductDetailsInOrder() {
        routes.clear();
        routes.put("/product/1/similarids", req -> json("[\"2\",\"3\"]"));
        routes.put("/product/2", req -> json("""
                {"id":"2","name":"Dress","price":19.99,"availability":true}
                """));
        routes.put("/product/3", req -> json("""
                {"id":"3","name":"Blazer","price":29.99,"availability":false}
                """));

        webTestClient.get()
                .uri("/product/1/similar")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("2")
                .jsonPath("$[0].name").isEqualTo("Dress")
                .jsonPath("$[0].price").isEqualTo(19.99)
                .jsonPath("$[0].availability").isEqualTo(true)
                .jsonPath("$[1].id").isEqualTo("3")
                .jsonPath("$[1].name").isEqualTo("Blazer")
                .jsonPath("$[1].availability").isEqualTo(false);
    }

    @Test
    void returns404WhenProductHasNoSimilarIds() {
        routes.clear();
        routes.put("/product/99/similarids", req -> new MockResponse().setResponseCode(404));

        webTestClient.get()
                .uri("/product/99/similar")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found: 99");
    }

    @Test
    void skipsFailedSimilarDetailsAndReturnsTheRest() {
        routes.clear();
        routes.put("/product/10/similarids", req -> json("[\"2\",\"5\"]"));
        routes.put("/product/2", req -> json("""
                {"id":"2","name":"Dress","price":19.99,"availability":true}
                """));
        routes.put("/product/5", req -> new MockResponse().setResponseCode(500));

        webTestClient.get()
                .uri("/product/10/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("2")
                .jsonPath("$[1]").doesNotExist();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}

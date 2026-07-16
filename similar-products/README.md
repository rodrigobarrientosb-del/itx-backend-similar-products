# similar-products

Servicio en Java 17 / Spring Boot 3 (WebFlux) que expone:

```
GET /product/{productId}/similar
```

en el puerto **5000**.

Flujo simple:

1. Pide los ids similares al upstream.
2. Pide el detalle de cada uno (en paralelo, manteniendo el orden).
3. Devuelve la lista. Si un detalle falla o se pasa de tiempo, lo salta y sigue con el resto.

## Cómo está montado

Arquitectura hexagonal, sin inventar más capas de las necesarias:

- Entrada: controlador REST.
- Caso de uso: juntar ids + detalles.
- Salida: puertos para “dame similares” y “dame ficha”, con adaptadores HTTP + caché encima.

```
Controller → GetSimilarProductsUseCase
                ↓
         SimilarIdsPort / ProductDetailPort
                ↓
     WebClient (+ timeout, circuit breaker)
                ↓
           Caché Caffeine
```

## Stack

- WebFlux / Reactor
- Resilience4j (circuit breaker)
- Caffeine (caché en memoria con TTL)
- JUnit + Mockito + StepVerifier en los tests

## Arranque

Desde la raíz del repo (un nivel arriba de esta carpeta):

```bash
docker compose up -d simulado influxdb grafana
```

Aquí:

```bash
mvn spring-boot:run
# o, si ya compilaste:
# java -jar target/similar-products-1.0.0.jar
```

Tests:

```bash
mvn test
```

k6 (app escuchando en 5000):

```bash
cd ..
docker compose run --rm k6 run scripts/test.js
```

## Por qué estas decisiones

Lo escribí pensando en lo que pide la prueba: resiliencia y rendimiento bajo carga, no solo el “happy path”.

- **WebFlux**: el test de k6 mete 200 VUs; I/O no bloqueante ayuda.
- **Timeout de 2 s**: el mock tiene productos con delay de hasta 50 s. No tiene sentido esperarlos.
- **Circuit breaker**: si el upstream empieza a devolver 500, dejamos de martillarlo un rato.
- **Caché ~2 min**: en el load test se piden una y otra vez los mismos ids; los datos no cambian.
- **Si un similar falla, lo omito**: mejor devolver lo que sí salió que tumbar toda la respuesta.
- **404 solo si fallan los similar-ids**: encaja con el “Product Not found” del OpenAPI para el producto pedido.

## Config

En `src/main/resources/application.yml`:

| Clave | Default | Para qué |
|-------|---------|----------|
| `app.upstream.base-url` | `http://localhost:3001` | Mocks |
| `app.upstream.timeout-ms` | `2000` | Corte por llamada |
| `app.cache.*` | TTL 120 s | Caché de ids y fichas |

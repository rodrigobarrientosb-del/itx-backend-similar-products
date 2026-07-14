# Similar Products — Backend

API REST de productos similares (puerto **5000**).

## Arranque

```bash
docker compose up -d simulado influxdb grafana
cd similar-products && mvn spring-boot:run
curl http://localhost:5000/product/1/similar
```

## Decisiones

- WebFlux + timeout 2s + circuit breaker.
- Caché Caffeine (~2 min) porque el load test repite los mismos ids.

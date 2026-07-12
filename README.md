# Similar Products — Backend

API REST de productos similares (puerto **5000**).

## Arranque

```bash
docker compose up -d simulado influxdb grafana
cd similar-products && mvn spring-boot:run
```

## Decisiones hasta ahora

- WebFlux para aguantar la carga del k6.
- Timeout 2s y circuit breaker frente a un upstream lento o inestable.

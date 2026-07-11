# Similar Products — Backend

API REST de productos similares (puerto **5000**).

## Arranque

```bash
docker compose up -d simulado influxdb grafana
cd similar-products && mvn spring-boot:run
```

## Diseño (en progreso)

- Dominio + puertos hexagonales.
- Caso de uso: pide ids similares, luego fichas en paralelo; si una falla, la omite.

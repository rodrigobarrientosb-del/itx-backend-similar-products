# Similar Products — Backend

API REST que, dado un producto, devuelve el detalle de sus similares.

Orquesta dos servicios que ya existen (ids similares + ficha de producto) y publica el contrato de `similarProducts.yaml` en el puerto **5000**.

## Carpetas

| Ruta | Qué es |
|------|--------|
| `similar-products/` | La app (Spring Boot) |
| `shared/` | Mocks, k6 y Grafana (vienen con la prueba) |
| `similarProducts.yaml` | Contrato que hay que cumplir |
| `existingApis.yaml` | APIs upstream |
| `docker-compose.yaml` | Mocks + infra del test de carga |

## Cómo probarlo en local

```bash
# 1. Mocks (y stack para ver métricas de k6)
docker compose up -d simulado influxdb grafana
curl http://localhost:3001/product/1/similarids

# 2. La app
cd similar-products
mvn spring-boot:run

# 3. Smoke
curl http://localhost:5000/product/1/similar

# 4. Carga (con la app en el 5000)
cd ..
docker compose run --rm k6 run scripts/test.js
```

Grafana: http://localhost:3000/d/Le2Ku9NMk/k6-performance-test

Más detalle de diseño y config: [`similar-products/README.md`](./similar-products/README.md).

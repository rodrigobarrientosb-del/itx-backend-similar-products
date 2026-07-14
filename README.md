# Similar Products — Backend

API REST que, dado un producto, devuelve el detalle de sus similares.

Contrato: `similarProducts.yaml` · Puerto: **5000**.

## Arranque

```bash
docker compose up -d simulado influxdb grafana
cd similar-products && mvn spring-boot:run
curl http://localhost:5000/product/1/similar
docker compose run --rm k6 run scripts/test.js
```

Endpoint listo: `GET /product/{productId}/similar`.

Detalle de diseño: [`similar-products/README.md`](./similar-products/README.md) (cuando esté).

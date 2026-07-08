# Similar Products — Backend

API REST de productos similares (prueba técnica).

## Carpetas

| Ruta | Qué es |
|------|--------|
| `shared/` | Mocks, k6 y Grafana |
| `similarProducts.yaml` | Contrato a cumplir |
| `existingApis.yaml` | APIs upstream |
| `docker-compose.yaml` | Infra de prueba |

## Cómo levantar los mocks

```bash
docker compose up -d simulado influxdb grafana
curl http://localhost:3001/product/1/similarids
```

Siguiente paso: la app Spring Boot en el puerto 5000.

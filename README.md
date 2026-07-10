# Similar Products — Backend

API REST de productos similares (puerto **5000**).

## Arranque

```bash
docker compose up -d simulado influxdb grafana
cd similar-products && mvn spring-boot:run
```

## Diseño (en progreso)

Arquitectura hexagonal: modelo `Product`, excepción de no encontrado y puertos
de entrada/salida. Siguiente: caso de uso e infraestructura.

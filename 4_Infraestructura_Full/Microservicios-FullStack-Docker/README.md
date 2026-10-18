# Microservicios Full Stack - Docker

Incluye:

- PostgreSQL 18.6 para ventas
- PostgreSQL 18.6 para pagos
- Tomcat 11.0.27 con JDK 25
- ventas-service.war
- pagos-service.war
- Nginx 1.31.7 Alpine
- Frontend Angular estático servido por Nginx
- app-config.json externo para las URLs del backend

## Puertos

| Servicio | Host |
|---|---|
| Angular / Nginx | http://localhost:81 |
| Tomcat | http://localhost:8080 |
| PostgreSQL Ventas | localhost:5434 |
| PostgreSQL Pagos | localhost:5435 |

## 1. Copiar los WAR

Genera cada WAR:

```powershell
mvn clean install
```

Copia:

```text
ventas-service/target/ventas-service.war
pagos-service/target/pagos-service.war
```

a:

```text
wars/
```

## 2. Copiar el build de Angular

En ventas-front:

```powershell
ng build
```

Si genera:

```text
dist/ventas-front/browser/
```

copia EL CONTENIDO de esa carpeta dentro de:

```text
frontend/
```

## 3. Configuración externa del frontend

El archivo:

```text
config/app-config.json
```

contiene:

```json
{
  "ventasApiUrl": "http://localhost:8080/ventas-service/api",
  "pagosApiUrl": "http://localhost:8080/pagos-service/api"
}
```

Nginx lo monta en:

```text
/usr/share/nginx/html/config/app-config.json
```

Puedes cambiar las URLs sin volver a ejecutar `ng build`.

## 4. Levantar todo

```powershell
docker compose up -d
```

## 5. Ver estado

```powershell
docker compose ps
```

## 6. Ver logs

Tomcat:

```powershell
docker logs -f tomcat-microservicios
```

Nginx:

```powershell
docker logs -f nginx-ventas-front
```

## 7. URLs

Frontend:

```text
http://localhost:81
http://localhost:81/dashboard
http://localhost:81/productos
http://localhost:81/ventas
http://localhost:81/pagos
```

Backend:

```text
http://localhost:8080/ventas-service/api/productos
http://localhost:8080/ventas-service/api/ventas
http://localhost:8080/pagos-service/api/pagos
```

## 8. Detener

```powershell
docker compose down
```

## 9. Destruir contenedores y bases

```powershell
docker compose down -v
```

## 10. Recrear bases de datos

Los scripts de `docker-entrypoint-initdb.d` se ejecutan solamente con volúmenes vacíos.

Para recrear:

```powershell
docker compose down -v
docker compose up -d
```

## 11. Reemplazar los WAR

Después de recompilar un microservicio, reemplaza su WAR dentro de `wars/` y ejecuta:

```powershell
docker compose down
docker compose up -d
```

## 12. Reemplazar el frontend

Después de ejecutar nuevamente:

```powershell
ng build
```

reemplaza el contenido de `frontend/` y reinicia Nginx:

```powershell
docker compose restart nginx
```

## Nota sobre app-config.json

Como `config/app-config.json` está montado como volumen, no necesitas copiar ese archivo desde `dist` ni reconstruir Angular para cambiar las URLs del backend.

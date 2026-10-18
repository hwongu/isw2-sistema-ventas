# Microservicios - Docker

Este proyecto levanta:

- PostgreSQL para `ventas-service`
- PostgreSQL para `pagos-service`
- Apache Tomcat 11 con JDK 25
- Los dos WAR se copian manualmente dentro de la carpeta `wars`

## Estructura

```text
Microservicios-Docker/
├── docker-compose.yml
├── ventas/
│   └── init/
│       ├── 01-schema.sql
│       └── 02-data.sql
├── pagos/
│   └── init/
│       ├── 01-schema.sql
│       └── 02-data.sql
├── wars/
│   └── LEEME.txt
└── properties-ejemplo/
    ├── ventas-database.properties
    ├── ventas-application.properties
    └── pagos-database.properties
```

## 1. Compilar los microservicios

En `ventas-service`:

```powershell
mvn clean install
```

Debe generar:

```text
target/ventas-service.war
```

En `pagos-service`:

```powershell
mvn clean install
```

Debe generar:

```text
target/pagos-service.war
```

## 2. Copiar los WAR

Copiar ambos dentro de:

```text
wars/
```

Debe quedar:

```text
wars/
├── ventas-service.war
└── pagos-service.war
```

## 3. Configuracion JDBC dentro de los WAR

Cuando las aplicaciones se ejecutan dentro de Tomcat en Docker, NO deben usar `localhost`
para conectarse a PostgreSQL.

### ventas-service

```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://postgres-ventas:5432/ventasdb
db.user=postgres
db.password=clave
```

### pagos-service

```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://postgres-pagos:5432/pagosdb
db.user=postgres
db.password=clave
```

### Comunicacion ventas -> pagos

Como ambos WAR se ejecutan dentro del mismo Tomcat:

```properties
pago.service.url=http://localhost:8080/pagos-service/api/pagos
```

## 4. Levantar todo

Desde esta carpeta:

```powershell
docker compose up -d
```

Verificar:

```powershell
docker compose ps
```

## 5. Ver logs de Tomcat

```powershell
docker logs -f tomcat-microservicios
```

## 6. URLs

Productos:

```text
http://localhost:8080/ventas-service/api/productos
```

Ventas:

```text
http://localhost:8080/ventas-service/api/ventas
```

Pagos:

```text
http://localhost:8080/pagos-service/api/pagos
```

## 7. Acceso a PostgreSQL desde localhost

Ventas:

```text
Host: localhost
Puerto: 5434
Base de datos: ventasdb
Usuario: postgres
Clave: clave
```

Pagos:

```text
Host: localhost
Puerto: 5435
Base de datos: pagosdb
Usuario: postgres
Clave: clave
```

## 8. Detener

```powershell
docker compose down
```

Esto conserva los datos.

## 9. Eliminar todo, incluyendo los datos

```powershell
docker compose down -v
```

Los scripts SQL de `docker-entrypoint-initdb.d` se ejecutan solo al inicializar un volumen
vacío. Si se modifican los scripts y se desea recrear las bases:

```powershell
docker compose down -v
docker compose up -d
```

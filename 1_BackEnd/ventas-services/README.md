# ventas-service

Microservicio educativo para Ingeniería de Software II usando Java puro, Jakarta Servlet y JDBC.

## 1) Requisitos

- JDK 25
- Apache Maven 3.10.0
- PostgreSQL 18.6
- Apache Tomcat 11.0.x (compatible con 11.0.27)

## 2) Crear base de datos

```sql
CREATE DATABASE ventasdb;
```

## 3) Ejecutar `schema.sql`

Archivo: `src/main/resources/sql/schema.sql`

Ejecutar el script en PostgreSQL para crear tablas:

- `producto`
- `venta`
- `detalle_venta`

## 4) Ejecutar `data.sql`

Archivo: `src/main/resources/sql/data.sql`

Carga productos iniciales de ejemplo.

## 5) Configurar conexión

Editar `src/main/resources/database.properties` según su entorno:

- `db.url`
- `db.user`
- `db.password`

## 6) Compilación

```bash
mvn clean install
```

## 7) Resultado esperado

Debe generarse:

`target/ventas-service.war`

## 8) Despliegue

Copiar el WAR a:

`<TOMCAT_HOME>/webapps`

Ejemplo en Windows:

```bat
copy target\ventas-service.war C:\apache-tomcat-11\webapps\
```

## 9) Iniciar Tomcat

Iniciar el servidor Apache Tomcat 11.

## 10) Probar endpoints

GET productos:

`http://localhost:8080/ventas-service/api/productos`

GET ventas:

`http://localhost:8080/ventas-service/api/ventas`

## Notas de diseño pedagógico

- SRP: responsabilidades separadas por capa (`controller`, `service`, `dao`).
- DIP: servicios dependen de interfaces DAO y cliente de pagos.
- DAO: SQL aislado en implementaciones JDBC.
- Configuración externa: conexión y URL de pagos en `.properties`.
- Microservicios: `ventas-service` no procesa pagos internamente; invoca `pagos-service` por HTTP.

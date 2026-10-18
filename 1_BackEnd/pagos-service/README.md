# pagos-service

Microservicio educativo de pagos para el curso de Ingeniería de Software II.  
Implementado con Java puro, Jakarta Servlet y JDBC, sin Spring ni ORM.

## 1) Requisitos

- JDK 25
- Apache Maven 3.10.0
- PostgreSQL 18.6
- Apache Tomcat 11.0.x (compatible con 11.0.27)

## 2) Crear base de datos

```sql
CREATE DATABASE pagosdb;
```

## 3) Ejecutar `schema.sql`

Archivo: `src/main/resources/sql/schema.sql`

## 4) Ejecutar opcionalmente `data.sql`

Archivo: `src/main/resources/sql/data.sql`

## 5) Configurar conexión

Archivo: `src/main/resources/database.properties`

```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://postgres-pagos:5432/pagosdb
db.user=postgres
db.password=clave
```

## 6) Compilar y empaquetar

```bash
mvn clean install
```

## 7) Resultado esperado

Se genera:

`target/pagos-service.war`

## 8) Despliegue en Tomcat

Copiar el WAR a:

`<TOMCAT_HOME>/webapps`

Ejemplo Windows:

```bat
copy target\pagos-service.war C:\apache-tomcat-11\webapps\
```

## 9) Iniciar Tomcat

Levantar Tomcat de forma externa (no embebida).

## 10) Endpoint base

`http://localhost:8080/pagos-service/api/pagos`

## 11) Probar listado

```http
GET http://localhost:8080/pagos-service/api/pagos
```

## 12) Probar pago efectivo

```http
POST http://localhost:8080/pagos-service/api/pagos
Content-Type: application/json

{
  "ventaId": 1001,
  "monto": 100.00,
  "metodoPago": "EFECTIVO"
}
```

## 13) Probar pago con tarjeta aprobado

```http
POST http://localhost:8080/pagos-service/api/pagos
Content-Type: application/json

{
  "ventaId": 1002,
  "monto": 250.00,
  "metodoPago": "TARJETA",
  "tokenTarjeta": "VALIDA"
}
```

## 14) Probar pago con tarjeta rechazado

```http
POST http://localhost:8080/pagos-service/api/pagos
Content-Type: application/json

{
  "ventaId": 1003,
  "monto": 80.00,
  "metodoPago": "TARJETA",
  "tokenTarjeta": "INVALIDA"
}
```

## 15) Flujo de capas

Controller → Service (Context) → Strategy (Efectivo/Tarjeta) → DAO → PostgreSQL

## 16) Principios aplicados

- SRP: responsabilidades separadas por capa.
- DIP: `PagoServiceImpl` depende de `PagoDAO` (interfaz), no de implementación concreta.
- DAO: acceso a datos aislado en `PagoDAOImpl`.
- Strategy: lógica variable encapsulada en `PagoEfectivo` y `PagoTarjeta`.
- Independencia de microservicios: `pagos-service` usa solo `pagosdb`.

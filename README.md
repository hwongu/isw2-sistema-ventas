
# Sistema de Ventas – Full Stack

**Universidad de Lima | Ingeniería de Software II **

Proyecto académico desarrollado para demostrar la aplicación de programación orientada a objetos, principios SOLID, patrones de diseño, arquitectura de software, integración mediante API REST y despliegue con Docker.

El sistema permite gestionar productos, registrar ventas, procesar pagos y consultar información comercial desde una aplicación web desarrollada con Angular y Java 25.

---

## 1. Tecnologías utilizadas

| Capa | Tecnologías |
|---|---|
| Frontend | Angular, TypeScript, Angular Material |
| Backend | Java 25, Jakarta Servlet, Maven |
| Servidor de aplicaciones | Apache Tomcat 11 |
| Bases de datos | PostgreSQL 18.6 |
| Servidor web | Nginx 1.31.7 Alpine |
| Infraestructura | Docker, Docker Compose |
| Integración | API REST, HTTP, JSON |
| Persistencia | JDBC, patrón DAO |
| Patrones de diseño | Strategy, DAO |

El backend se desarrolla sin Spring Boot ni frameworks ORM, con el propósito de comprender directamente las responsabilidades y la integración de cada capa.

---

## 2. Estructura del repositorio

```text
isw2-sistema-ventas-fullstack/
│
├── 1_BackEnd/
│   └── Código fuente de ventas-service y pagos-service
│
├── 2_Infraestructura_BackEnd/
│   └── Infraestructura Docker del backend
│
├── 3_FrontEnd/
│   └── Código fuente Angular
│
├── 4_Infraestructura_Full/
│   └── Infraestructura Docker de la solución completa
│
└── README.md
```

### Descripción de las carpetas

**1_BackEnd**

Contiene los proyectos Java Maven correspondientes a los servicios:

- `ventas-service`: gestión de productos y ventas.
- `pagos-service`: procesamiento y consulta de pagos.

**2_Infraestructura_BackEnd**

Contiene los archivos de infraestructura necesarios para ejecutar los servicios backend y sus bases de datos PostgreSQL utilizando Docker Compose.

**3_FrontEnd**

Contiene el proyecto Angular encargado de proporcionar la interfaz de usuario del Sistema de Ventas.

**4_Infraestructura_Full**

Contiene la infraestructura necesaria para ejecutar la solución completa, incluyendo frontend, backend y bases de datos.

---

## 3. Arquitectura de la solución

El Sistema de Ventas está organizado en tres componentes principales:

### ventas-front

Aplicación web desarrollada con Angular.

Responsabilidades:

- Consultar el Dashboard.
- Gestionar productos.
- Registrar y consultar ventas.
- Procesar y consultar pagos.
- Consumir las API REST de los servicios backend.

### ventas-service

Servicio backend desarrollado con Java 25 y Jakarta Servlet.

Responsabilidades:

- Gestionar productos.
- Validar precios y stock.
- Registrar ventas y sus detalles.
- Consultar el historial de ventas.
- Coordinar el procesamiento automático de pagos en efectivo mediante `pagos-service`.

Utiliza la base de datos PostgreSQL `ventasdb`.

### pagos-service

Servicio backend desarrollado con Java 25 y Jakarta Servlet.

Responsabilidades:

- Registrar pagos en efectivo.
- Procesar pagos con tarjeta simulada.
- Consultar operaciones de pago.
- Buscar pagos asociados a una venta.
- Registrar el resultado de las operaciones.

Utiliza la base de datos PostgreSQL `pagosdb`.

### Comunicación entre componentes

```text
                +------------------+
                |   ventas-front   |
                |     Angular      |
                +------------------+
                   |           |
              REST/JSON    REST/JSON
                   |           |
                   v           v
          +----------------+  +----------------+
          | ventas-service |  | pagos-service  |
          |    Java 25     |->|    Java 25     |
          +----------------+  +----------------+
                   |                  |
                  JDBC               JDBC
                   |                  |
                   v                  v
              +---------+        +---------+
              | ventasdb|        | pagosdb |
              +---------+        +---------+
```

`ventas-service` también consume la API REST de `pagos-service` para procesar automáticamente el pago correspondiente al registro de una venta.

---

## 4. Patrones de diseño y principios aplicados

### Arquitectura en capas

Los servicios backend siguen una separación de responsabilidades:

```text
Controller
    |
    v
Service
    |
    v
DAO
    |
    v
PostgreSQL
```

### DAO (Data Access Object)

Permite encapsular las operaciones de acceso a datos utilizando JDBC y PostgreSQL.

Se implementa mediante interfaces y clases concretas organizadas en paquetes `impl`.

### Strategy

Se utiliza en `pagos-service` para implementar diferentes estrategias de pago.

- `EstrategiaPago`: interfaz principal.
- `PagoEfectivo`: procesamiento de pagos en efectivo.
- `PagoTarjeta`: procesamiento de pagos con tarjeta simulada.

### Principios SOLID

Se aplican principios de diseño orientado a objetos para separar responsabilidades, reducir el acoplamiento y facilitar el mantenimiento del código.

---

## 5. Funcionalidades principales

### EP01 – Dashboard

- Visualizar resumen de productos.
- Visualizar resumen de ventas.
- Visualizar resumen de pagos.

### EP02 – Gestión de productos

- Listar productos.
- Registrar productos.
- Actualizar productos.
- Eliminar productos.
- Consultar detalle de productos.

### EP03 – Gestión de ventas

- Consultar historial de ventas.
- Preparar el detalle de una venta.
- Registrar ventas.
- Validar stock disponible.
- Procesar automáticamente el pago en efectivo.
- Consultar detalle de ventas.

### EP04 – Gestión de pagos

- Listar pagos.
- Buscar pagos por identificador de venta.
- Procesar pagos en efectivo.
- Procesar pagos con tarjeta simulada.
- Consultar detalle de pagos.

---

## 6. Infraestructura Docker

La solución completa utiliza cuatro contenedores Docker:

| Contenedor | Tecnología | Puerto del anfitrión |
|---|---|---|
| nginx-ventas-front | Nginx 1.31.7 Alpine | 81 |
| tomcat-microservicios | Tomcat 11 / Java 25 | 8080 |
| postgres-ventas | PostgreSQL 18.6 | 5434 |
| postgres-pagos | PostgreSQL 18.6 | 5435 |

Ambos servicios Java se despliegan como archivos WAR dentro de una misma instancia de Apache Tomcat.

Las bases de datos PostgreSQL se ejecutan en contenedores independientes.

### Persistencia

Se utilizan dos volúmenes Docker:

- `postgres_ventas_data`
- `postgres_pagos_data`

Estos volúmenes permiten conservar los datos cuando se recrean los contenedores.

---

## 7. Ejecución de la solución completa

### Requisitos previos

- Docker Desktop o Docker Engine con Docker Compose.
- Puertos 81, 8080, 5434 y 5435 disponibles.
- Archivos WAR de ambos servicios previamente compilados.
- Archivos compilados del frontend Angular.

### Paso 1. Preparar los artefactos

En la infraestructura completa deben estar disponibles:

```text
4_Infraestructura_Full/
│
├── docker-compose.yml
│
├── wars/
│   ├── ventas-service.war
│   └── pagos-service.war
│
├── frontend/
│   └── Archivos compilados de Angular
│
├── nginx/
│   └── default.conf
│
├── config/
│   └── app-config.json
│
├── ventas/
│   └── init/
│
└── pagos/
    └── init/
```

### Paso 2. Iniciar los contenedores

Desde la carpeta `4_Infraestructura_Full`, ejecutar:

```bash
docker compose up -d
```

### Paso 3. Verificar el estado

```bash
docker compose ps
```

### Paso 4. Consultar los logs

```bash
docker compose logs -f
```

Para consultar los logs de Tomcat:

```bash
docker compose logs -f tomcat
```

### Paso 5. Detener la infraestructura

```bash
docker compose down
```

Este comando elimina los contenedores y la red creada por Compose, pero conserva los volúmenes persistentes.

**Importante:** evitar `docker compose down -v` si se desea conservar la información almacenada en PostgreSQL.

---

## 8. URLs de acceso

### Frontend

```text
http://localhost:81
```

### API de productos

```text
http://localhost:8080/ventas-service/api/productos
```

### API de ventas

```text
http://localhost:8080/ventas-service/api/ventas
```

### API de pagos

```text
http://localhost:8080/pagos-service/api/pagos
```

Las direcciones `localhost` corresponden al acceso desde el equipo que ejecuta Docker.

Para acceder desde otro equipo, será necesario configurar las direcciones de los servicios de acuerdo con la red utilizada.

---

## 9. Ejemplos de consumo REST

### Consultar productos

**GET**

```http
GET /ventas-service/api/productos
```

### Registrar producto

**POST**

```http
POST /ventas-service/api/productos
Content-Type: application/json
```

```json
{
  "codigo": "PROD001",
  "nombre": "Teclado",
  "precio": 80.00,
  "stock": 20
}
```

### Registrar venta

**POST**

```http
POST /ventas-service/api/ventas
Content-Type: application/json
```

```json
{
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 2
    }
  ]
}
```

El backend valida el stock, calcula los importes y solicita automáticamente el pago en efectivo a `pagos-service`.

### Registrar pago manual en efectivo

**POST**

```http
POST /pagos-service/api/pagos
Content-Type: application/json
```

```json
{
  "ventaId": 1001,
  "monto": 150.75,
  "metodoPago": "EFECTIVO"
}
```

### Registrar pago manual con tarjeta simulada

**POST**

```http
POST /pagos-service/api/pagos
Content-Type: application/json
```

```json
{
  "ventaId": 1001,
  "monto": 150.75,
  "metodoPago": "TARJETA",
  "tokenTarjeta": "VALIDA"
}
```

El token `VALIDA` representa una operación aprobada en esta simulación académica.

---

## 10. Documentación arquitectónica

El proyecto permite estudiar y aplicar técnicas de documentación de arquitectura de software.

### UML 2.5

- Diagrama de casos de uso.
- Diagrama de paquetes.
- Diagrama de componentes.
- Diagrama de secuencia.
- Diagrama de despliegue.

### Modelo C4

- Nivel 1: Contexto del sistema.
- Nivel 2: Contenedores.
- Nivel 3: Componentes.

### Diseño arquitectónico

- Identificación de stakeholders.
- Historias de usuario.
- Requisitos arquitectónicamente significativos (ASR).
- Atributos de calidad del software.
- Utility Tree.
- Escenarios de atributos de calidad.
- Metas y restricciones arquitectónicas.
- Decisiones de arquitectura.

---

## 11. Limitaciones conocidas

El sistema fue desarrollado con fines académicos y no está preparado para su utilización en producción sin mejoras adicionales.

Se deben considerar las siguientes limitaciones:

- Los pagos con tarjeta son simulados.
- No existe integración con una pasarela bancaria real.
- No se ha implementado autenticación y autorización completas.
- El entorno local utiliza HTTP.
- No se dispone de transacciones distribuidas entre `ventasdb` y `pagosdb`.
- No se han implementado mecanismos completos de idempotencia y compensación automática.
- Ambos servicios backend comparten una misma instancia de Tomcat.
- Los valores de configuración y credenciales de laboratorio deben reemplazarse para otros entornos.

Estas limitaciones constituyen oportunidades de análisis y mejora arquitectónica.

---

## 12. Propósito académico

El objetivo principal del proyecto es proporcionar un caso práctico e integrado para aplicar conocimientos de Ingeniería de Software II.

Los estudiantes podrán relacionar el código implementado con los principios de diseño, los patrones utilizados, los atributos de calidad y las decisiones arquitectónicas que afectan el funcionamiento del sistema.

---

**Universidad de Lima**

**Ingeniería de Software II – 2026-2**

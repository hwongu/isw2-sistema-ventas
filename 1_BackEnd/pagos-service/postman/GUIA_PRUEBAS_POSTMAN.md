# Guía de Pruebas en Postman - `pagos-service`

Esta guía resume **todos los servicios expuestos** y cómo probarlos en Postman.
El servicio expone operaciones de consulta y registro de pagos; **no expone PUT/DELETE** por diseño de dominio.

## 1) URL base

Si desplegaste el WAR con el nombre por defecto:

`http://localhost:8080/pagos-service`

Endpoint base del API:

`{{baseUrl}}/api/pagos`

---

## 2) Preparación rápida

1. Compilar y generar WAR:
   - `mvn clean install`
2. Copiar `target/pagos-service.war` a `<TOMCAT_HOME>/webapps`.
3. Levantar Tomcat.
4. En Postman, crear un Environment con variable:
   - `baseUrl = http://localhost:8080/pagos-service`

---

## 3) Servicios expuestos

## 3.1 Crear pago

- **Método:** `POST`
- **URL:** `{{baseUrl}}/api/pagos`
- **Headers:**
  - `Content-Type: application/json`
- **Body:** `raw` + `JSON`

### Caso A: Pago en efectivo (aprobado)

```json
{
  "ventaId": 1001,
  "monto": 150.75,
  "metodoPago": "EFECTIVO"
}
```

**Respuesta esperada (201 Created):**

```json
{
  "pagoId": 1,
  "ventaId": 1001,
  "monto": 150.75,
  "metodoPago": "EFECTIVO",
  "estado": "APROBADO",
  "codigoOperacion": "PAGO-123456",
  "mensaje": "Pago procesado correctamente"
}
```

> Nota: `codigoOperacion` lo genera el backend y sigue el formato `PAGO-######`.

### Caso B: Pago con tarjeta aprobado

```json
{
  "ventaId": 1002,
  "monto": 220.00,
  "metodoPago": "TARJETA",
  "tokenTarjeta": "VALIDA"
}
```

**Respuesta esperada (201 Created):**

```json
{
  "pagoId": 2,
  "ventaId": 1002,
  "monto": 220.00,
  "metodoPago": "TARJETA",
  "estado": "APROBADO",
  "codigoOperacion": "PAGO-654321",
  "mensaje": "Pago procesado correctamente"
}
```

> Nota: `codigoOperacion` es generado por el backend; el valor exacto varía.

### Caso C: Pago con tarjeta rechazado

```json
{
  "ventaId": 1003,
  "monto": 80.00,
  "metodoPago": "TARJETA",
  "tokenTarjeta": "INVALIDA"
}
```

**Respuesta esperada (422 Unprocessable Entity):**

```json
{
  "pagoId": 3,
  "ventaId": 1003,
  "monto": 80.00,
  "metodoPago": "TARJETA",
  "estado": "RECHAZADO",
  "codigoOperacion": "PAGO-111222",
  "mensaje": "Pago rechazado por token de tarjeta inválido"
}
```

Este caso es un **resultado de negocio** (tarjeta rechazada), no un error técnico 500.

---

## 3.2 Listar pagos

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/pagos`
- **Body:** No aplica

**Respuesta esperada (200 OK):**

```json
[
  {
    "id": 1,
    "ventaId": 1001,
    "fecha": "2026-10-14T10:15:30",
    "monto": 150.75,
    "metodoPago": "EFECTIVO",
    "estado": "APROBADO",
    "codigoOperacion": "PAGO-123456"
  }
]
```

---

## 3.3 Buscar pago por ID

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/pagos/{id}`
- **Ejemplo URL:** `{{baseUrl}}/api/pagos/1`
- **Body:** No aplica

**Respuesta esperada (200 OK):**

```json
{
  "id": 1,
  "ventaId": 1001,
  "fecha": "2026-10-14T10:15:30",
  "monto": 150.75,
  "metodoPago": "EFECTIVO",
  "estado": "APROBADO",
  "codigoOperacion": "PAGO-123456"
}
```

Si no existe:
- **404 Not Found**

```json
{
  "error": "Pago no encontrado con id: 99999"
}
```

---

## 3.4 Buscar pagos por `ventaId`

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/pagos?ventaId=1001`
- **Body:** No aplica

**Respuesta esperada (200 OK):**

```json
[
  {
    "id": 1,
    "ventaId": 1001,
    "fecha": "2026-10-14T10:15:30",
    "monto": 150.75,
    "metodoPago": "EFECTIVO",
    "estado": "APROBADO",
    "codigoOperacion": "PAGO-123456"
  }
]
```

---

## 4) Validaciones y errores comunes

## 4.1 Estructura de error

Cuando hay error de negocio/validación:

```json
{
  "error": "mensaje descriptivo"
}
```

## 4.2 Casos frecuentes

- `ventaId` nulo o `<= 0`:
  - `400 Bad Request`
- `metodoPago` vacío:
  - `400 Bad Request`
- `metodoPago` no soportado (solo `EFECTIVO`, `TARJETA`):
  - `400 Bad Request`
- `monto` nulo o `<= 0`:
  - `400 Bad Request`
- `tokenTarjeta` faltante para `TARJETA`:
  - `400 Bad Request`
- `id` de ruta no numérico (`GET /api/pagos/{id}`):
  - `400 Bad Request`
- `ventaId` query no numérico (`GET /api/pagos?ventaId=abc`):
  - `400 Bad Request`

---

## 5) Set sugerido de requests en Postman

Crea una colección `pagos-service` con este orden:

1. `POST - Crear pago efectivo`
2. `POST - Crear pago tarjeta (aprobado)`
3. `POST - Crear pago tarjeta (rechazado)`
4. `GET - Listar pagos`
5. `GET - Obtener pago por ID`
6. `GET - Buscar por ventaId`

Con esto cubres flujo feliz + flujo rechazado + consultas + validaciones.

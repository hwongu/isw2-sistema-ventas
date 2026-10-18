# Guía de Pruebas en Postman - `ventas-service`

Esta guía refleja el comportamiento real del código de `ventas-service` (Servlet + JDBC + Jackson).

## 1) URL base

Si despliegas el WAR con el nombre por defecto:

`http://localhost:8080/ventas-service`

API:

- Productos: `{{baseUrl}}/api/productos`
- Ventas: `{{baseUrl}}/api/ventas`

## 2) Preparación

1. Ejecutar `schema.sql` y `data.sql`.
2. Verificar `database.properties`:
   - `db.url=jdbc:postgresql://postgres-ventas:5432/ventasdb`
3. Verificar `application.properties`:
   - `pago.service.url=http://localhost:8080/pagos-service/api/pagos`
4. Compilar: `mvn clean install`
5. Desplegar `target/ventas-service.war` en Tomcat.
6. En Postman, crear Environment:
   - `baseUrl = http://localhost:8080/ventas-service`
   - `productoId = 1`
   - `ventaId = 1`

## 3) Productos

## 3.1 Listar productos

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/productos`
- **Headers:** No requeridos
- **Body:** No aplica
- **Status esperado:** `200 OK`

**Response ejemplo:**

```json
[
  {
    "id": 1,
    "codigo": "P-001",
    "nombre": "Laptop Básica",
    "precio": 2500.00,
    "stock": 15
  }
]
```

## 3.2 Buscar producto por ID

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/productos/{{productoId}}`
- **Headers:** No requeridos
- **Body:** No aplica
- **Status esperado:** `200 OK`

**Response ejemplo:**

```json
{
  "id": 1,
  "codigo": "P-001",
  "nombre": "Laptop Básica",
  "precio": 2500.00,
  "stock": 15
}
```

Si no existe:
- **Status esperado:** `404 Not Found`

```json
{
  "error": "Producto no encontrado con id: 99999"
}
```

## 3.3 Crear producto

- **Método:** `POST`
- **URL:** `{{baseUrl}}/api/productos`
- **Headers:**
  - `Content-Type: application/json`
- **Body:** `raw` + `JSON`
- **Status esperado:** `201 Created`

**Request ejemplo:**

```json
{
  "codigo": "P-010",
  "nombre": "Webcam HD",
  "precio": 199.90,
  "stock": 30
}
```

**Response ejemplo:**

```json
{
  "id": 6,
  "codigo": "P-010",
  "nombre": "Webcam HD",
  "precio": 199.90,
  "stock": 30
}
```

## 3.4 Actualizar producto

- **Método:** `PUT`
- **URL:** `{{baseUrl}}/api/productos/{{productoId}}`
- **Headers:**
  - `Content-Type: application/json`
- **Body:** `raw` + `JSON`
- **Status esperado:** `200 OK`

**Request ejemplo:**

```json
{
  "codigo": "P-010",
  "nombre": "Webcam Full HD",
  "precio": 210.00,
  "stock": 25
}
```

**Response ejemplo:**

```json
{
  "id": 6,
  "codigo": "P-010",
  "nombre": "Webcam Full HD",
  "precio": 210.00,
  "stock": 25
}
```

## 3.5 Eliminar producto

- **Método:** `DELETE`
- **URL:** `{{baseUrl}}/api/productos/{{productoId}}`
- **Headers:** No requeridos
- **Body:** No aplica
- **Status esperado:** `204 No Content`

Si el producto está asociado a `detalle_venta`:
- **Status esperado:** `400 Bad Request`

```json
{
  "error": "No se puede eliminar el producto porque tiene ventas asociadas"
}
```

## 4) Ventas

## 4.1 Listar ventas

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/ventas`
- **Headers:** No requeridos
- **Body:** No aplica
- **Status esperado:** `200 OK`

**Response ejemplo (estructura):**

```json
[
  {
    "id": 1,
    "fecha": "2026-10-14T10:15:30",
    "total": 5181.80,
    "estado": "PAGADA",
    "detalles": [
      {
        "id": 1,
        "ventaId": 1,
        "productoId": 1,
        "cantidad": 2,
        "precioUnitario": 2500.00,
        "subtotal": 5000.00
      }
    ]
  }
]
```

## 4.2 Buscar venta por ID

- **Método:** `GET`
- **URL:** `{{baseUrl}}/api/ventas/{{ventaId}}`
- **Headers:** No requeridos
- **Body:** No aplica
- **Status esperado:** `200 OK`

**Response ejemplo (estructura):**

```json
{
  "id": 1,
  "fecha": "2026-10-14T10:15:30",
  "total": 5181.80,
  "estado": "PAGADA",
  "detalles": [
    {
      "id": 1,
      "ventaId": 1,
      "productoId": 1,
      "cantidad": 2,
      "precioUnitario": 2500.00,
      "subtotal": 5000.00
    }
  ]
}
```

Si no existe:
- **Status esperado:** `404 Not Found`

```json
{
  "error": "Venta no encontrada con id: 99999"
}
```

## 4.3 Registrar venta

- **Método:** `POST`
- **URL:** `{{baseUrl}}/api/ventas`
- **Headers:**
  - `Content-Type: application/json`
- **Body:** `raw` + `JSON`
- **Status esperado:** `201 Created`

**Request ejemplo:**

```json
{
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 2
    },
    {
      "productoId": 3,
      "cantidad": 1
    }
  ]
}
```

**Response ejemplo (estructura):**

```json
{
  "id": 2,
  "fecha": "2026-10-14T11:00:00",
  "total": 5180.50,
  "estado": "PAGADA",
  "detalles": [
    {
      "id": 3,
      "ventaId": 2,
      "productoId": 1,
      "cantidad": 2,
      "precioUnitario": 2500.00,
      "subtotal": 5000.00
    },
    {
      "id": 4,
      "ventaId": 2,
      "productoId": 3,
      "cantidad": 1,
      "precioUnitario": 180.50,
      "subtotal": 180.50
    }
  ]
}
```

**Nota importante de contrato actual:**
- `POST /api/ventas` **no** recibe `metodoPago` ni `tokenTarjeta`.
- `POST /api/ventas` **no** recibe `moneda` ni `descripcion`.
- El backend envía a `pagos-service`:
  - `ventaId = id generado de la venta`
  - `monto = total calculado`
  - `metodoPago = "EFECTIVO"`
- Integración interna:
  - `POST {{pago.service.url}}`
  - Body JSON:

```json
{
  "ventaId": 15,
  "monto": 3200.00,
  "metodoPago": "EFECTIVO"
}
```

## 4.4 Otras operaciones reales

Actualmente **no existen** endpoints de:
- anulación de venta
- consulta de comprobante

## 5) Validaciones

## 5.1 Producto

- `codigo`: obligatorio y no vacío.
- `nombre`: obligatorio y no vacío.
- `precio`: obligatorio y `>= 0`.
- `stock`: obligatorio y `>= 0`.
- `codigo` duplicado: `400 Bad Request`.

## 5.2 Venta

- Debe tener al menos un detalle.
- Cada detalle debe existir y tener:
  - `productoId` obligatorio y `> 0`
  - `cantidad` obligatoria y `> 0`
- El producto debe existir.
- Debe haber stock suficiente.
- El precio se toma del producto en BD, no del request.

## 6) Errores HTTP

Formato de error:

```json
{
  "error": "mensaje descriptivo"
}
```

Estados usados por el API:

- `200 OK`
- `201 Created`
- `204 No Content`
- `400 Bad Request`
- `404 Not Found`
- `500 Internal Server Error`

Casos comunes:

- JSON inválido → `400`
- ID no numérico o ruta inválida → `400`
- Recurso no encontrado → `404`
- Pago rechazado por `pagos-service` → `400`
- Fallo interno no controlado → `500`

## 7) Flujo sugerido de pruebas

1. `GET /api/productos`
2. `POST /api/productos` (producto de prueba)
3. `PUT /api/productos/{id}`
4. `GET /api/productos/{id}`
5. `POST /api/ventas`
6. `GET /api/ventas`
7. `GET /api/ventas/{id}`
8. `DELETE /api/productos/{id}` (solo si no está referenciado por ventas)

-- ============================================================
-- pagosdb - Datos iniciales
-- ============================================================

INSERT INTO pago (
    venta_id,
    fecha,
    monto,
    metodo_pago,
    estado,
    codigo_operacion
)
VALUES
    (100, CURRENT_TIMESTAMP, 150.50, 'EFECTIVO', 'APROBADO', 'PAGO-000001'),
    (101, CURRENT_TIMESTAMP, 250.00, 'TARJETA', 'APROBADO', 'PAGO-000002')
ON CONFLICT (codigo_operacion) DO NOTHING;

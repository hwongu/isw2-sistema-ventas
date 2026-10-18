-- ============================================================
-- ventasdb - Datos iniciales
-- ============================================================

INSERT INTO producto (codigo, nombre, precio, stock)
VALUES
    ('PROD-001', 'Laptop UlimaBook 14', 3200.00, 10),
    ('PROD-002', 'Monitor 24 pulgadas', 850.00, 20),
    ('PROD-003', 'Teclado mecanico', 280.00, 35),
    ('PROD-004', 'Mouse inalambrico', 120.00, 50),
    ('PROD-005', 'Webcam Full HD', 190.00, 25)
ON CONFLICT (codigo) DO NOTHING;

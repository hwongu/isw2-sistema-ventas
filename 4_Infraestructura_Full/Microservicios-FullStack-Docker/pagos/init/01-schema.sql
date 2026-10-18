CREATE TABLE IF NOT EXISTS pago (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    metodo_pago VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    codigo_operacion VARCHAR(50) UNIQUE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pago_venta_id
    ON pago(venta_id);

CREATE INDEX IF NOT EXISTS idx_pago_estado
    ON pago(estado);

export interface Pago {
  id: number;
  ventaId: number;
  fecha: string;
  monto: number;
  metodoPago: string;
  estado: string;
  codigoOperacion: string;
}

export interface PagoRequest {
  ventaId: number;
  monto: number;
  metodoPago: 'EFECTIVO' | 'TARJETA';
  tokenTarjeta?: string;
}

export interface PagoResponse {
  pagoId: number;
  ventaId: number;
  monto: number;
  metodoPago: string;
  estado: string;
  codigoOperacion: string;
  mensaje: string;
}

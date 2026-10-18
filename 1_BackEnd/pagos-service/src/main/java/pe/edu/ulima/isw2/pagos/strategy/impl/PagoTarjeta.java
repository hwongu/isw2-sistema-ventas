package pe.edu.ulima.isw2.pagos.strategy.impl;

import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;
import pe.edu.ulima.isw2.pagos.exception.PagoException;
import pe.edu.ulima.isw2.pagos.strategy.EstrategiaPago;

import java.math.BigDecimal;

public class PagoTarjeta implements EstrategiaPago {
    @Override
    public PagoResponse procesar(PagoRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagoException("El monto para pago con tarjeta debe ser mayor que cero", 400);
        }
        if (request.getTokenTarjeta() == null || request.getTokenTarjeta().isBlank()) {
            throw new PagoException("El token de tarjeta es obligatorio para pagos con tarjeta", 400);
        }

        boolean aprobado = "VALIDA".equalsIgnoreCase(request.getTokenTarjeta());

        PagoResponse response = new PagoResponse();
        response.setVentaId(request.getVentaId());
        response.setMonto(request.getMonto());
        response.setMetodoPago("TARJETA");
        response.setEstado(aprobado ? "APROBADO" : "RECHAZADO");
        response.setCodigoOperacion(generarCodigoTemporal());
        response.setMensaje(aprobado ? "Pago procesado correctamente" : "Pago rechazado por token de tarjeta inválido");
        return response;
    }

    private String generarCodigoTemporal() {
        int valor = Math.abs((int) (System.nanoTime() % 1_000_000));
        return String.format("PAGO-%06d", valor);
    }
}

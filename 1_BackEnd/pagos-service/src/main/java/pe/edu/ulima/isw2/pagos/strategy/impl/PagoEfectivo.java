package pe.edu.ulima.isw2.pagos.strategy.impl;

import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;
import pe.edu.ulima.isw2.pagos.exception.PagoException;
import pe.edu.ulima.isw2.pagos.strategy.EstrategiaPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoEfectivo implements EstrategiaPago {
    @Override
    public PagoResponse procesar(PagoRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagoException("El monto para pago en efectivo debe ser mayor que cero", 400);
        }

        PagoResponse response = new PagoResponse();
        response.setVentaId(request.getVentaId());
        response.setMonto(request.getMonto());
        response.setMetodoPago("EFECTIVO");
        response.setEstado("APROBADO");
        response.setCodigoOperacion(generarCodigoTemporal());
        response.setMensaje("Pago procesado correctamente");
        return response;
    }

    private String generarCodigoTemporal() {
        int valor = Math.abs((int) (System.nanoTime() % 1_000_000));
        return String.format("PAGO-%06d", valor);
    }
}

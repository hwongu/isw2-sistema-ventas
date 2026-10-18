package pe.edu.ulima.isw2.pagos.service;

import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;
import pe.edu.ulima.isw2.pagos.entity.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoService {
    PagoResponse procesarPago(PagoRequest request);

    Optional<Pago> buscarPorId(Long id);

    List<Pago> listar();

    List<Pago> buscarPorVentaId(Long ventaId);
}

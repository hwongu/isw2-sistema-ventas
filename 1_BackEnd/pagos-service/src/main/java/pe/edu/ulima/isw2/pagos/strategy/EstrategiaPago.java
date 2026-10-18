package pe.edu.ulima.isw2.pagos.strategy;

import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;

public interface EstrategiaPago {
    PagoResponse procesar(PagoRequest request);
}

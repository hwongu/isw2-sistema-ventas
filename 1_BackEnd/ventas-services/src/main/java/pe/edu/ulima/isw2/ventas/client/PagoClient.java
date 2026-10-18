package pe.edu.ulima.isw2.ventas.client;

import pe.edu.ulima.isw2.ventas.dto.PagoRequest;
import pe.edu.ulima.isw2.ventas.dto.PagoResponse;

public interface PagoClient {
    PagoResponse procesarPago(PagoRequest request);
}

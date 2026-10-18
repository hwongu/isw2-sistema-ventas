package pe.edu.ulima.isw2.ventas.dto;

import java.util.ArrayList;
import java.util.List;

public class VentaRequest {
    private List<DetalleVentaRequest> detalles = new ArrayList<>();

    public VentaRequest() {
    }

    public List<DetalleVentaRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaRequest> detalles) {
        this.detalles = detalles;
    }
}

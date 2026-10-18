package pe.edu.ulima.isw2.ventas.service;

import pe.edu.ulima.isw2.ventas.dto.VentaRequest;
import pe.edu.ulima.isw2.ventas.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> listar();

    Venta buscarPorId(Long id);

    Venta registrarVenta(VentaRequest request);
}

package pe.edu.ulima.isw2.ventas.dao;

import pe.edu.ulima.isw2.ventas.entity.Venta;

import java.util.Optional;

public interface VentaDAO extends Dao<Long, Venta> {
    Optional<Venta> buscarConDetallesPorId(Long id);
}

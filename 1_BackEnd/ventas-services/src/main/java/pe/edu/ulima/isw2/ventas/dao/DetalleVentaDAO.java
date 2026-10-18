package pe.edu.ulima.isw2.ventas.dao;

import pe.edu.ulima.isw2.ventas.entity.DetalleVenta;

import java.util.List;

public interface DetalleVentaDAO extends Dao<Long, DetalleVenta> {
    List<DetalleVenta> listarPorVentaId(Long ventaId);
}

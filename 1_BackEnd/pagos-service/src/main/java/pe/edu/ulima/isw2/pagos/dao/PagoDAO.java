package pe.edu.ulima.isw2.pagos.dao;

import pe.edu.ulima.isw2.pagos.entity.Pago;

import java.util.List;

public interface PagoDAO extends Dao<Long, Pago> {
    List<Pago> buscarPorVentaId(Long ventaId);

    List<Pago> buscarPorEstado(String estado);
}

package pe.edu.ulima.isw2.ventas.dao;

import pe.edu.ulima.isw2.ventas.entity.Producto;

import java.util.Optional;

public interface ProductoDAO extends Dao<Long, Producto> {
    Optional<Producto> buscarPorCodigo(String codigo);
}

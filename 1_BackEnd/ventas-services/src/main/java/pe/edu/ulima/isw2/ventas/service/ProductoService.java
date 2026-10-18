package pe.edu.ulima.isw2.ventas.service;

import pe.edu.ulima.isw2.ventas.entity.Producto;

import java.util.List;

public interface ProductoService {
    List<Producto> listar();

    Producto buscarPorId(Long id);

    Producto registrar(Producto producto);

    Producto actualizar(Long id, Producto producto);

    void eliminar(Long id);
}

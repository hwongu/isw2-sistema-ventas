package pe.edu.ulima.isw2.ventas.service.impl;

import pe.edu.ulima.isw2.ventas.dao.ProductoDAO;
import pe.edu.ulima.isw2.ventas.entity.Producto;
import pe.edu.ulima.isw2.ventas.exception.RecursoNoEncontradoException;
import pe.edu.ulima.isw2.ventas.service.ProductoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoServiceImpl implements ProductoService {
    private final ProductoDAO productoDAO;

    public ProductoServiceImpl(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    @Override
    public List<Producto> listar() {
        return productoDAO.listar();
    }

    @Override
    public Producto buscarPorId(Long id) {
        return productoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
    }

    @Override
    public Producto registrar(Producto producto) {
        validarProducto(producto);
        validarCodigoDisponible(producto.getCodigo(), null);
        return productoDAO.insertar(producto);
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        validarProducto(producto);
        Producto existente = buscarPorId(id);
        validarCodigoDisponible(producto.getCodigo(), id);
        existente.setCodigo(producto.getCodigo());
        existente.setNombre(producto.getNombre());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());

        boolean actualizado = productoDAO.actualizar(existente);
        if (!actualizado) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + id);
        }
        return existente;
    }

    @Override
    public void eliminar(Long id) {
        boolean eliminado = productoDAO.eliminar(id);
        if (!eliminado) {
            throw new RecursoNoEncontradoException("Producto no encontrado con id: " + id);
        }
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null) {
            throw new IllegalArgumentException("El precio del producto es obligatorio");
        }
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor o igual a 0");
        }
        if (producto.getStock() == null) {
            throw new IllegalArgumentException("El stock del producto es obligatorio");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock del producto debe ser mayor o igual a 0");
        }
    }

    private void validarCodigoDisponible(String codigo, Long idActual) {
        Optional<Producto> productoConCodigo = productoDAO.buscarPorCodigo(codigo);
        if (productoConCodigo.isEmpty()) {
            return;
        }
        Producto existente = productoConCodigo.get();
        if (idActual == null || !existente.getId().equals(idActual)) {
            throw new IllegalArgumentException("Ya existe un producto con código: " + codigo);
        }
    }
}

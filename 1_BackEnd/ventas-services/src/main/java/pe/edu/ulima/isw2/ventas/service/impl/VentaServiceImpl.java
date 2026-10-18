package pe.edu.ulima.isw2.ventas.service.impl;

import pe.edu.ulima.isw2.ventas.client.PagoClient;
import pe.edu.ulima.isw2.ventas.config.ConexionBD;
import pe.edu.ulima.isw2.ventas.dao.DetalleVentaDAO;
import pe.edu.ulima.isw2.ventas.dao.ProductoDAO;
import pe.edu.ulima.isw2.ventas.dao.VentaDAO;
import pe.edu.ulima.isw2.ventas.dao.impl.DetalleVentaDAOImpl;
import pe.edu.ulima.isw2.ventas.dao.impl.ProductoDAOImpl;
import pe.edu.ulima.isw2.ventas.dao.impl.VentaDAOImpl;
import pe.edu.ulima.isw2.ventas.dto.DetalleVentaRequest;
import pe.edu.ulima.isw2.ventas.dto.PagoRequest;
import pe.edu.ulima.isw2.ventas.dto.PagoResponse;
import pe.edu.ulima.isw2.ventas.dto.VentaRequest;
import pe.edu.ulima.isw2.ventas.entity.DetalleVenta;
import pe.edu.ulima.isw2.ventas.entity.Producto;
import pe.edu.ulima.isw2.ventas.entity.Venta;
import pe.edu.ulima.isw2.ventas.exception.RecursoNoEncontradoException;
import pe.edu.ulima.isw2.ventas.exception.VentaException;
import pe.edu.ulima.isw2.ventas.service.VentaService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaServiceImpl implements VentaService {
    private final VentaDAO ventaDAO;
    private final DetalleVentaDAO detalleVentaDAO;
    private final ProductoDAO productoDAO;
    private final PagoClient pagoClient;
    private final VentaDAOImpl ventaDaoTx;
    private final DetalleVentaDAOImpl detalleVentaDaoTx;
    private final ProductoDAOImpl productoDaoTx;

    public VentaServiceImpl(VentaDAO ventaDAO, DetalleVentaDAO detalleVentaDAO, ProductoDAO productoDAO, PagoClient pagoClient) {
        this.ventaDAO = ventaDAO;
        this.detalleVentaDAO = detalleVentaDAO;
        this.productoDAO = productoDAO;
        this.pagoClient = pagoClient;
        this.ventaDaoTx = obtenerImplementacion(ventaDAO, VentaDAOImpl.class, "VentaDAO");
        this.detalleVentaDaoTx = obtenerImplementacion(detalleVentaDAO, DetalleVentaDAOImpl.class, "DetalleVentaDAO");
        this.productoDaoTx = obtenerImplementacion(productoDAO, ProductoDAOImpl.class, "ProductoDAO");
    }

    @Override
    public List<Venta> listar() {
        List<Venta> ventas = ventaDAO.listar();
        for (Venta venta : ventas) {
            venta.setDetalles(detalleVentaDAO.listarPorVentaId(venta.getId()));
        }
        return ventas;
    }

    @Override
    public Venta buscarPorId(Long id) {
        return ventaDAO.buscarConDetallesPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));
    }

    @Override
    public Venta registrarVenta(VentaRequest request) {
        validarRequest(request);

        List<DetalleCalculado> detallesCalculados = calcularDetalles(request.getDetalles());
        BigDecimal total = calcularTotal(detallesCalculados);

        return persistirVentaConTransaccion(detallesCalculados, total);
    }

    private void validarRequest(VentaRequest request) {
        if (request == null || request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new VentaException("La venta debe contener al menos un detalle");
        }
    }

    private List<DetalleCalculado> calcularDetalles(List<DetalleVentaRequest> detallesRequest) {
        List<DetalleCalculado> detallesCalculados = new ArrayList<>();
        for (DetalleVentaRequest detalleRequest : detallesRequest) {
            if (detalleRequest == null) {
                throw new VentaException("Cada detalle de la venta es obligatorio");
            }
            if (detalleRequest.getProductoId() == null || detalleRequest.getProductoId() <= 0) {
                throw new VentaException("El productoId del detalle es obligatorio y debe ser mayor a cero");
            }

            Integer cantidad = detalleRequest.getCantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new VentaException("La cantidad del producto debe ser mayor a cero");
            }

            Producto producto = productoDAO.buscarPorId(detalleRequest.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + detalleRequest.getProductoId()));

            if (producto.getStock() < cantidad) {
                throw new VentaException("Stock insuficiente para el producto: " + producto.getCodigo());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            detallesCalculados.add(new DetalleCalculado(producto, cantidad, subtotal));
        }
        return detallesCalculados;
    }

    private BigDecimal calcularTotal(List<DetalleCalculado> detallesCalculados) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCalculado detalleCalculado : detallesCalculados) {
            total = total.add(detalleCalculado.subtotal());
        }
        return total;
    }

    private Venta persistirVentaConTransaccion(List<DetalleCalculado> detallesCalculados, BigDecimal total) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try {
                Venta venta = new Venta();
                venta.setFecha(LocalDateTime.now());
                venta.setTotal(total);
                venta.setEstado("PAGADA");
                Venta ventaGuardada = ventaDaoTx.insertar(connection, venta);

                PagoRequest pagoRequest = new PagoRequest(ventaGuardada.getId(), total, "EFECTIVO");
                PagoResponse pagoResponse = pagoClient.procesarPago(pagoRequest);
                if (pagoResponse == null || !pagoResponse.isAprobado()) {
                    throw new VentaException("Pago rechazado por pagos-service");
                }

                List<DetalleVenta> detallesGuardados = new ArrayList<>();
                for (DetalleCalculado detalleCalculado : detallesCalculados) {
                    Producto productoActual = productoDaoTx.buscarPorId(connection, detalleCalculado.producto().getId())
                            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + detalleCalculado.producto().getId()));

                    if (productoActual.getStock() < detalleCalculado.cantidad()) {
                        throw new VentaException("Stock insuficiente para el producto: " + productoActual.getCodigo());
                    }

                    DetalleVenta detalleVenta = new DetalleVenta();
                    detalleVenta.setVentaId(ventaGuardada.getId());
                    detalleVenta.setProductoId(productoActual.getId());
                    detalleVenta.setCantidad(detalleCalculado.cantidad());
                    detalleVenta.setPrecioUnitario(productoActual.getPrecio());
                    detalleVenta.setSubtotal(detalleCalculado.subtotal());
                    DetalleVenta detalleGuardado = detalleVentaDaoTx.insertar(connection, detalleVenta);
                    detallesGuardados.add(detalleGuardado);

                    productoActual.setStock(productoActual.getStock() - detalleCalculado.cantidad());
                    productoDaoTx.actualizar(connection, productoActual);
                }

                connection.commit();
                ventaGuardada.setDetalles(detallesGuardados);
                return ventaGuardada;
            } catch (RuntimeException e) {
                rollbackSilencioso(connection);
                throw e;
            } catch (Exception e) {
                rollbackSilencioso(connection);
                throw new VentaException("Error al registrar la venta", e);
            }
        } catch (SQLException e) {
            throw new VentaException("Error de conexión al registrar la venta", e);
        }
    }

    private void rollbackSilencioso(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new VentaException("Error al revertir la transacción de venta", e);
        }
    }

    private <T> T obtenerImplementacion(Object dependencia, Class<T> tipoEsperado, String nombreDependencia) {
        if (!tipoEsperado.isInstance(dependencia)) {
            throw new IllegalStateException(nombreDependencia + " debe usar implementación " + tipoEsperado.getSimpleName());
        }
        return tipoEsperado.cast(dependencia);
    }

    private record DetalleCalculado(Producto producto, Integer cantidad, BigDecimal subtotal) {
    }
}

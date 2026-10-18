package pe.edu.ulima.isw2.ventas.dao.impl;

import pe.edu.ulima.isw2.ventas.config.ConexionBD;
import pe.edu.ulima.isw2.ventas.dao.DetalleVentaDAO;
import pe.edu.ulima.isw2.ventas.entity.DetalleVenta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetalleVentaDAOImpl implements DetalleVentaDAO {

    @Override
    public Optional<DetalleVenta> buscarPorId(Long id) {
        String sql = "SELECT id, venta_id, producto_id, cantidad, precio_unitario, subtotal FROM detalle_venta WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearDetalle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar detalle de venta por id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<DetalleVenta> listar() {
        String sql = "SELECT id, venta_id, producto_id, cantidad, precio_unitario, subtotal FROM detalle_venta ORDER BY id";
        List<DetalleVenta> detalles = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                detalles.add(mapearDetalle(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar detalles de venta", e);
        }
        return detalles;
    }

    @Override
    public DetalleVenta insertar(DetalleVenta entidad) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return insertar(connection, entidad);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar detalle de venta", e);
        }
    }

    @Override
    public boolean actualizar(DetalleVenta entidad) {
        String sql = "UPDATE detalle_venta SET venta_id = ?, producto_id = ?, cantidad = ?, precio_unitario = ?, subtotal = ? WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, entidad.getVentaId());
            ps.setLong(2, entidad.getProductoId());
            ps.setInt(3, entidad.getCantidad());
            ps.setBigDecimal(4, entidad.getPrecioUnitario());
            ps.setBigDecimal(5, entidad.getSubtotal());
            ps.setLong(6, entidad.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar detalle de venta", e);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        String sql = "DELETE FROM detalle_venta WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar detalle de venta", e);
        }
    }

    @Override
    public List<DetalleVenta> listarPorVentaId(Long ventaId) {
        String sql = "SELECT id, venta_id, producto_id, cantidad, precio_unitario, subtotal FROM detalle_venta WHERE venta_id = ? ORDER BY id";
        List<DetalleVenta> detalles = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapearDetalle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar detalles por venta", e);
        }
        return detalles;
    }

    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        return new DetalleVenta(
                rs.getLong("id"),
                rs.getLong("venta_id"),
                rs.getLong("producto_id"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unitario"),
                rs.getBigDecimal("subtotal")
        );
    }

    public DetalleVenta insertar(Connection connection, DetalleVenta entidad) {
        String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entidad.getVentaId());
            ps.setLong(2, entidad.getProductoId());
            ps.setInt(3, entidad.getCantidad());
            ps.setBigDecimal(4, entidad.getPrecioUnitario());
            ps.setBigDecimal(5, entidad.getSubtotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                }
            }
            return entidad;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar detalle de venta", e);
        }
    }
}

package pe.edu.ulima.isw2.ventas.dao.impl;

import pe.edu.ulima.isw2.ventas.config.ConexionBD;
import pe.edu.ulima.isw2.ventas.dao.VentaDAO;
import pe.edu.ulima.isw2.ventas.entity.DetalleVenta;
import pe.edu.ulima.isw2.ventas.entity.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaDAOImpl implements VentaDAO {

    @Override
    public Optional<Venta> buscarPorId(Long id) {
        String sql = "SELECT id, fecha, total, estado FROM venta WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearVenta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar venta por id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Venta> listar() {
        String sql = "SELECT id, fecha, total, estado FROM venta ORDER BY id";
        List<Venta> ventas = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas", e);
        }
        return ventas;
    }

    @Override
    public Venta insertar(Venta entidad) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return insertar(connection, entidad);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar venta", e);
        }
    }

    @Override
    public boolean actualizar(Venta entidad) {
        String sql = "UPDATE venta SET fecha = ?, total = ?, estado = ? WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(entidad.getFecha()));
            ps.setBigDecimal(2, entidad.getTotal());
            ps.setString(3, entidad.getEstado());
            ps.setLong(4, entidad.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar venta", e);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        String sql = "DELETE FROM venta WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar venta", e);
        }
    }

    @Override
    public Optional<Venta> buscarConDetallesPorId(Long id) {
        Optional<Venta> ventaOptional = buscarPorId(id);
        if (ventaOptional.isEmpty()) {
            return Optional.empty();
        }
        Venta venta = ventaOptional.get();
        venta.setDetalles(obtenerDetallesPorVentaId(id));
        return Optional.of(venta);
    }

    private List<DetalleVenta> obtenerDetallesPorVentaId(Long ventaId) {
        String sql = "SELECT id, venta_id, producto_id, cantidad, precio_unitario, subtotal FROM detalle_venta WHERE venta_id = ? ORDER BY id";
        List<DetalleVenta> detalles = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new DetalleVenta(
                            rs.getLong("id"),
                            rs.getLong("venta_id"),
                            rs.getLong("producto_id"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("subtotal")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar detalles de la venta", e);
        }
        return detalles;
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getLong("id"));
        Timestamp timestamp = rs.getTimestamp("fecha");
        venta.setFecha(timestamp != null ? timestamp.toLocalDateTime() : null);
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setEstado(rs.getString("estado"));
        return venta;
    }

    public Venta insertar(Connection connection, Venta entidad) {
        String sql = "INSERT INTO venta (fecha, total, estado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(entidad.getFecha()));
            ps.setBigDecimal(2, entidad.getTotal());
            ps.setString(3, entidad.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                }
            }
            return entidad;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar venta", e);
        }
    }
}

package pe.edu.ulima.isw2.ventas.dao.impl;

import pe.edu.ulima.isw2.ventas.config.ConexionBD;
import pe.edu.ulima.isw2.ventas.dao.ProductoDAO;
import pe.edu.ulima.isw2.ventas.entity.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAOImpl implements ProductoDAO {

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por id", e);
        }
    }

    @Override
    public List<Producto> listar() {
        String sql = "SELECT id, codigo, nombre, precio, stock FROM producto ORDER BY id";
        List<Producto> productos = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos", e);
        }
        return productos;
    }

    @Override
    public Producto insertar(Producto entidad) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return insertar(connection, entidad);
        } catch (SQLException e) {
            throw mapearErrorSql("Error al insertar producto", e);
        }
    }

    @Override
    public boolean actualizar(Producto entidad) {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return actualizar(connection, entidad);
        } catch (SQLException e) {
            throw mapearErrorSql("Error al actualizar producto", e);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalStateException("No se puede eliminar el producto porque tiene ventas asociadas");
            }
            throw new RuntimeException("Error al eliminar producto", e);
        }
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        String sql = "SELECT id, codigo, nombre, precio, stock FROM producto WHERE codigo = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por código", e);
        }
        return Optional.empty();
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getLong("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock")
        );
    }

    public Optional<Producto> buscarPorId(Connection connection, Long id) {
        String sql = "SELECT id, codigo, nombre, precio, stock FROM producto WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por id", e);
        }
        return Optional.empty();
    }

    public Producto insertar(Connection connection, Producto entidad) {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setBigDecimal(3, entidad.getPrecio());
            ps.setInt(4, entidad.getStock());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                }
            }
            return entidad;
        } catch (SQLException e) {
            throw mapearErrorSql("Error al insertar producto", e);
        }
    }

    public boolean actualizar(Connection connection, Producto entidad) {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, precio = ?, stock = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setBigDecimal(3, entidad.getPrecio());
            ps.setInt(4, entidad.getStock());
            ps.setLong(5, entidad.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw mapearErrorSql("Error al actualizar producto", e);
        }
    }

    private RuntimeException mapearErrorSql(String mensajeGenerico, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Ya existe un producto con el mismo código");
        }
        return new RuntimeException(mensajeGenerico, e);
    }
}

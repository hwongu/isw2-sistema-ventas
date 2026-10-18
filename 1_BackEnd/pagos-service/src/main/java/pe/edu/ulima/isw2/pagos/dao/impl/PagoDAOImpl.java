package pe.edu.ulima.isw2.pagos.dao.impl;

import pe.edu.ulima.isw2.pagos.config.ConexionBD;
import pe.edu.ulima.isw2.pagos.dao.PagoDAO;
import pe.edu.ulima.isw2.pagos.entity.Pago;
import pe.edu.ulima.isw2.pagos.exception.PagoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PagoDAOImpl implements PagoDAO {
    private static final String SQL_BUSCAR_POR_ID = """
            SELECT id, venta_id, fecha, monto, metodo_pago, estado, codigo_operacion
            FROM pago
            WHERE id = ?
            """;
    private static final String SQL_LISTAR = """
            SELECT id, venta_id, fecha, monto, metodo_pago, estado, codigo_operacion
            FROM pago
            ORDER BY id
            """;
    private static final String SQL_INSERTAR = """
            INSERT INTO pago (venta_id, fecha, monto, metodo_pago, estado, codigo_operacion)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String SQL_ACTUALIZAR = """
            UPDATE pago
            SET venta_id = ?, fecha = ?, monto = ?, metodo_pago = ?, estado = ?, codigo_operacion = ?
            WHERE id = ?
            """;
    private static final String SQL_ELIMINAR = "DELETE FROM pago WHERE id = ?";
    private static final String SQL_BUSCAR_POR_VENTA_ID = """
            SELECT id, venta_id, fecha, monto, metodo_pago, estado, codigo_operacion
            FROM pago
            WHERE venta_id = ?
            ORDER BY id
            """;
    private static final String SQL_BUSCAR_POR_ESTADO = """
            SELECT id, venta_id, fecha, monto, metodo_pago, estado, codigo_operacion
            FROM pago
            WHERE estado = ?
            ORDER BY id
            """;
    private static final String SQL_ACTUALIZAR_CODIGO = "UPDATE pago SET codigo_operacion = ? WHERE id = ?";

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearPago(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PagoException("Error al buscar pago por id", 500, exception);
        }
    }

    @Override
    public List<Pago> listar() {
        List<Pago> pagos = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                pagos.add(mapearPago(resultSet));
            }
            return pagos;
        } catch (SQLException exception) {
            throw new PagoException("Error al listar pagos", 500, exception);
        }
    }

    @Override
    public Pago insertar(Pago entidad) {
        String codigoTemporal = entidad.getCodigoOperacion();
        if (codigoTemporal == null || codigoTemporal.isBlank()) {
            codigoTemporal = "TMP-" + UUID.randomUUID();
        }

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, entidad.getVentaId());
            statement.setTimestamp(2, Timestamp.valueOf(entidad.getFecha()));
            statement.setBigDecimal(3, entidad.getMonto());
            statement.setString(4, entidad.getMetodoPago());
            statement.setString(5, entidad.getEstado());
            statement.setString(6, codigoTemporal);

            int filas = statement.executeUpdate();
            if (filas == 0) {
                throw new PagoException("No se pudo insertar el pago", 500);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGenerado = generatedKeys.getLong(1);
                    entidad.setId(idGenerado);
                    String codigoFinal = generarCodigoOperacion(idGenerado);
                    actualizarCodigoOperacion(connection, idGenerado, codigoFinal);
                    entidad.setCodigoOperacion(codigoFinal);
                    return entidad;
                }
                throw new PagoException("No se pudo recuperar el id del pago insertado", 500);
            }
        } catch (SQLException exception) {
            throw new PagoException("Error al insertar pago", 500, exception);
        }
    }

    @Override
    public boolean actualizar(Pago entidad) {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR)) {
            statement.setLong(1, entidad.getVentaId());
            statement.setTimestamp(2, Timestamp.valueOf(entidad.getFecha()));
            statement.setBigDecimal(3, entidad.getMonto());
            statement.setString(4, entidad.getMetodoPago());
            statement.setString(5, entidad.getEstado());
            statement.setString(6, entidad.getCodigoOperacion());
            statement.setLong(7, entidad.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new PagoException("Error al actualizar pago", 500, exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_ELIMINAR)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new PagoException("Error al eliminar pago", 500, exception);
        }
    }

    @Override
    public List<Pago> buscarPorVentaId(Long ventaId) {
        List<Pago> pagos = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_VENTA_ID)) {
            statement.setLong(1, ventaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pagos.add(mapearPago(resultSet));
                }
            }
            return pagos;
        } catch (SQLException exception) {
            throw new PagoException("Error al buscar pagos por ventaId", 500, exception);
        }
    }

    @Override
    public List<Pago> buscarPorEstado(String estado) {
        List<Pago> pagos = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_ESTADO)) {
            statement.setString(1, estado);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pagos.add(mapearPago(resultSet));
                }
            }
            return pagos;
        } catch (SQLException exception) {
            throw new PagoException("Error al buscar pagos por estado", 500, exception);
        }
    }

    private Pago mapearPago(ResultSet resultSet) throws SQLException {
        Pago pago = new Pago();
        pago.setId(resultSet.getLong("id"));
        pago.setVentaId(resultSet.getLong("venta_id"));
        Timestamp fecha = resultSet.getTimestamp("fecha");
        pago.setFecha(fecha != null ? fecha.toLocalDateTime() : LocalDateTime.now());
        pago.setMonto(resultSet.getBigDecimal("monto"));
        pago.setMetodoPago(resultSet.getString("metodo_pago"));
        pago.setEstado(resultSet.getString("estado"));
        pago.setCodigoOperacion(resultSet.getString("codigo_operacion"));
        return pago;
    }

    private void actualizarCodigoOperacion(Connection connection, Long id, String codigoOperacion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR_CODIGO)) {
            statement.setString(1, codigoOperacion);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    private String generarCodigoOperacion(long idGenerado) {
        return String.format("PAGO-%06d", idGenerado);
    }
}

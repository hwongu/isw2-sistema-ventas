package pe.edu.ulima.isw2.ventas.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {
    private static final String DB_DRIVER;
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = ConexionBD.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontró el archivo database.properties");
            }
            properties.load(inputStream);

            DB_DRIVER = properties.getProperty("db.driver");
            DB_URL = properties.getProperty("db.url");
            DB_USER = properties.getProperty("db.user");
            DB_PASSWORD = properties.getProperty("db.password");

            Class.forName(DB_DRIVER);
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error al cargar configuración de base de datos: " + e.getMessage());
        }
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}

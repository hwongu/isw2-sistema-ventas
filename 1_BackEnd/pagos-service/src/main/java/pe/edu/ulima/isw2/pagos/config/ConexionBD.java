package pe.edu.ulima.isw2.pagos.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {
    private static final String ARCHIVO_CONFIG = "database.properties";
    private static final Properties PROPIEDADES = new Properties();

    static {
        cargarPropiedades();
        cargarDriver();
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = PROPIEDADES.getProperty("db.url");
        String user = PROPIEDADES.getProperty("db.user");
        String password = PROPIEDADES.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static void cargarPropiedades() {
        ClassLoader classLoader = ConexionBD.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontró el archivo " + ARCHIVO_CONFIG);
            }
            PROPIEDADES.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIG, exception);
        }
    }

    private static void cargarDriver() {
        String driver = PROPIEDADES.getProperty("db.driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("No se pudo cargar el driver JDBC: " + driver, exception);
        }
    }
}

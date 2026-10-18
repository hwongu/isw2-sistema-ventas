package pe.edu.ulima.isw2.ventas.exception;

public class VentaException extends RuntimeException {
    public VentaException(String mensaje) {
        super(mensaje);
    }

    public VentaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

package pe.edu.ulima.isw2.pagos.exception;

public class PagoException extends RuntimeException {
    private final int statusCode;

    public PagoException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public PagoException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public PagoException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

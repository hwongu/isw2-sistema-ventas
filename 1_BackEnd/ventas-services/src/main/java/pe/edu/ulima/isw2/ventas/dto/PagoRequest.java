package pe.edu.ulima.isw2.ventas.dto;

import java.math.BigDecimal;

public class PagoRequest {
    private Long ventaId;
    private BigDecimal monto;
    private String metodoPago;

    public PagoRequest() {
    }

    public PagoRequest(Long ventaId, BigDecimal monto, String metodoPago) {
        this.ventaId = ventaId;
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}

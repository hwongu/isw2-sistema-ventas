package pe.edu.ulima.isw2.pagos.service.impl;

import pe.edu.ulima.isw2.pagos.dao.PagoDAO;
import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;
import pe.edu.ulima.isw2.pagos.entity.Pago;
import pe.edu.ulima.isw2.pagos.exception.PagoException;
import pe.edu.ulima.isw2.pagos.strategy.EstrategiaPago;
import pe.edu.ulima.isw2.pagos.strategy.impl.PagoEfectivo;
import pe.edu.ulima.isw2.pagos.strategy.impl.PagoTarjeta;
import pe.edu.ulima.isw2.pagos.service.PagoService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PagoServiceImpl implements PagoService {
    private final PagoDAO pagoDAO;
    private final Map<String, EstrategiaPago> estrategias;

    public PagoServiceImpl(PagoDAO pagoDAO) {
        this.pagoDAO = pagoDAO;
        this.estrategias = crearEstrategias();
    }

    public PagoServiceImpl(PagoDAO pagoDAO, Map<String, EstrategiaPago> estrategias) {
        this.pagoDAO = pagoDAO;
        this.estrategias = estrategias;
    }

    @Override
    public PagoResponse procesarPago(PagoRequest request) {
        validarRequest(request);

        String metodoPago = request.getMetodoPago().trim().toUpperCase();
        EstrategiaPago estrategiaPago = estrategias.get(metodoPago);
        if (estrategiaPago == null) {
            throw new PagoException("Metodo de pago no soportado: " + request.getMetodoPago(), 400);
        }

        PagoResponse resultadoEstrategia = estrategiaPago.procesar(request);

        Pago pago = new Pago();
        pago.setVentaId(request.getVentaId());
        pago.setFecha(LocalDateTime.now());
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(metodoPago);
        pago.setEstado(resultadoEstrategia.getEstado());
        pago.setCodigoOperacion(resultadoEstrategia.getCodigoOperacion());

        Pago pagoRegistrado = pagoDAO.insertar(pago);

        PagoResponse response = new PagoResponse();
        response.setPagoId(pagoRegistrado.getId());
        response.setVentaId(pagoRegistrado.getVentaId());
        response.setMonto(pagoRegistrado.getMonto());
        response.setMetodoPago(pagoRegistrado.getMetodoPago());
        response.setEstado(pagoRegistrado.getEstado());
        response.setCodigoOperacion(pagoRegistrado.getCodigoOperacion());
        response.setMensaje(resultadoEstrategia.getMensaje());
        return response;
    }

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new PagoException("El id debe ser mayor que cero", 400);
        }
        return pagoDAO.buscarPorId(id);
    }

    @Override
    public List<Pago> listar() {
        return pagoDAO.listar();
    }

    @Override
    public List<Pago> buscarPorVentaId(Long ventaId) {
        if (ventaId == null || ventaId <= 0) {
            throw new PagoException("El ventaId debe ser mayor que cero", 400);
        }
        return pagoDAO.buscarPorVentaId(ventaId);
    }

    private void validarRequest(PagoRequest request) {
        if (request == null) {
            throw new PagoException("El request de pago no puede ser nulo", 400);
        }
        if (request.getVentaId() == null || request.getVentaId() <= 0) {
            throw new PagoException("El ventaId es obligatorio y debe ser mayor que cero", 400);
        }
        if (request.getMetodoPago() == null || request.getMetodoPago().isBlank()) {
            throw new PagoException("El metodoPago es obligatorio", 400);
        }
    }

    private Map<String, EstrategiaPago> crearEstrategias() {
        Map<String, EstrategiaPago> mapa = new HashMap<>();
        mapa.put("EFECTIVO", new PagoEfectivo());
        mapa.put("TARJETA", new PagoTarjeta());
        return mapa;
    }
}

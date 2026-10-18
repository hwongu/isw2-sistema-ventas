package pe.edu.ulima.isw2.pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.ulima.isw2.pagos.dao.PagoDAO;
import pe.edu.ulima.isw2.pagos.dao.impl.PagoDAOImpl;
import pe.edu.ulima.isw2.pagos.dto.PagoRequest;
import pe.edu.ulima.isw2.pagos.dto.PagoResponse;
import pe.edu.ulima.isw2.pagos.entity.Pago;
import pe.edu.ulima.isw2.pagos.exception.PagoException;
import pe.edu.ulima.isw2.pagos.exception.RecursoNoEncontradoException;
import pe.edu.ulima.isw2.pagos.service.PagoService;
import pe.edu.ulima.isw2.pagos.service.impl.PagoServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/pagos/*")
public class PagoController extends HttpServlet {
    private transient PagoService pagoService;
    private transient ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        PagoDAO pagoDAO = new PagoDAOImpl();
        this.pagoService = new PagoServiceImpl(pagoDAO);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarRespuestaJson(response);
        try {
            PagoRequest pagoRequest = objectMapper.readValue(request.getInputStream(), PagoRequest.class);
            PagoResponse pagoResponse = pagoService.procesarPago(pagoRequest);
            if ("APROBADO".equalsIgnoreCase(pagoResponse.getEstado())) {
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.setStatus(422);
            }
            objectMapper.writeValue(response.getWriter(), pagoResponse);
        } catch (PagoException exception) {
            escribirError(response, exception.getStatusCode(), exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            escribirError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarRespuestaJson(response);
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                atenderListado(request, response);
                return;
            }

            Long id = parsearId(pathInfo);
            Optional<Pago> pago = pagoService.buscarPorId(id);
            if (pago.isEmpty()) {
                throw new RecursoNoEncontradoException("Pago no encontrado con id: " + id);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), pago.get());
        } catch (RecursoNoEncontradoException exception) {
            escribirError(response, HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        } catch (PagoException exception) {
            escribirError(response, exception.getStatusCode(), exception.getMessage());
        } catch (Exception exception) {
            escribirError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private void atenderListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ventaIdParametro = request.getParameter("ventaId");
        List<Pago> pagos;
        if (ventaIdParametro != null && !ventaIdParametro.isBlank()) {
            Long ventaId = parsearLong(ventaIdParametro, "El parámetro ventaId debe ser numérico");
            pagos = pagoService.buscarPorVentaId(ventaId);
        } else {
            pagos = pagoService.listar();
        }
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), pagos);
    }

    private Long parsearId(String pathInfo) {
        String valor = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (valor.contains("/")) {
            throw new PagoException("Ruta inválida para consultar pago", 400);
        }
        return parsearLong(valor, "El id del pago debe ser numérico");
    }

    private Long parsearLong(String valor, String mensajeError) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException exception) {
            throw new PagoException(mensajeError, 400);
        }
    }

    private void configurarRespuestaJson(HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
    }

    private void escribirError(HttpServletResponse response, int statusCode, String mensaje) throws IOException {
        response.setStatus(statusCode);
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", mensaje);
        objectMapper.writeValue(response.getWriter(), error);
    }
}

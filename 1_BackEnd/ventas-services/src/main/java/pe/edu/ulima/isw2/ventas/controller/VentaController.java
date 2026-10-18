package pe.edu.ulima.isw2.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.ulima.isw2.ventas.client.PagoClient;
import pe.edu.ulima.isw2.ventas.client.impl.PagoClientHttpImpl;
import pe.edu.ulima.isw2.ventas.dao.DetalleVentaDAO;
import pe.edu.ulima.isw2.ventas.dao.ProductoDAO;
import pe.edu.ulima.isw2.ventas.dao.VentaDAO;
import pe.edu.ulima.isw2.ventas.dao.impl.DetalleVentaDAOImpl;
import pe.edu.ulima.isw2.ventas.dao.impl.ProductoDAOImpl;
import pe.edu.ulima.isw2.ventas.dao.impl.VentaDAOImpl;
import pe.edu.ulima.isw2.ventas.dto.VentaRequest;
import pe.edu.ulima.isw2.ventas.entity.Venta;
import pe.edu.ulima.isw2.ventas.exception.RecursoNoEncontradoException;
import pe.edu.ulima.isw2.ventas.exception.VentaException;
import pe.edu.ulima.isw2.ventas.service.VentaService;
import pe.edu.ulima.isw2.ventas.service.impl.VentaServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/ventas/*")
public class VentaController extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private transient VentaService ventaService;

    @Override
    public void init() {
        VentaDAO ventaDAO = new VentaDAOImpl();
        DetalleVentaDAO detalleVentaDAO = new DetalleVentaDAOImpl();
        ProductoDAO productoDAO = new ProductoDAOImpl();
        PagoClient pagoClient = new PagoClientHttpImpl();
        this.ventaService = new VentaServiceImpl(ventaDAO, detalleVentaDAO, productoDAO, pagoClient);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                responderJson(response, HttpServletResponse.SC_OK, ventaService.listar());
                return;
            }
            Long id = extraerId(pathInfo);
            Venta venta = ventaService.buscarPorId(id);
            responderJson(response, HttpServletResponse.SC_OK, venta);
        } catch (RecursoNoEncontradoException e) {
            responderError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            responderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            VentaRequest ventaRequest = OBJECT_MAPPER.readValue(request.getInputStream(), VentaRequest.class);
            Venta ventaCreada = ventaService.registrarVenta(ventaRequest);
            responderJson(response, HttpServletResponse.SC_CREATED, ventaCreada);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, "JSON inválido");
        } catch (RecursoNoEncontradoException e) {
            responderError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (VentaException | IllegalArgumentException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            responderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Long extraerId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            throw new IllegalArgumentException("Debe enviar un id en la ruta");
        }
        String valor = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (valor.contains("/")) {
            throw new IllegalArgumentException("Ruta inválida");
        }
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El id debe ser numérico");
        }
    }

    private void responderJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    private void responderError(HttpServletResponse response, int status, String mensaje) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        responderJson(response, status, error);
    }
}

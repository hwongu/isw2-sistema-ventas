package pe.edu.ulima.isw2.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.ulima.isw2.ventas.dao.ProductoDAO;
import pe.edu.ulima.isw2.ventas.dao.impl.ProductoDAOImpl;
import pe.edu.ulima.isw2.ventas.entity.Producto;
import pe.edu.ulima.isw2.ventas.exception.RecursoNoEncontradoException;
import pe.edu.ulima.isw2.ventas.service.ProductoService;
import pe.edu.ulima.isw2.ventas.service.impl.ProductoServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/productos/*")
public class ProductoController extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private transient ProductoService productoService;

    @Override
    public void init() {
        ProductoDAO productoDAO = new ProductoDAOImpl();
        this.productoService = new ProductoServiceImpl(productoDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                responderJson(response, HttpServletResponse.SC_OK, productoService.listar());
                return;
            }

            Long id = extraerId(pathInfo);
            Producto producto = productoService.buscarPorId(id);
            responderJson(response, HttpServletResponse.SC_OK, producto);
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
            Producto producto = OBJECT_MAPPER.readValue(request.getInputStream(), Producto.class);
            Producto creado = productoService.registrar(producto);
            responderJson(response, HttpServletResponse.SC_CREATED, creado);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, "JSON inválido");
        } catch (IllegalArgumentException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            responderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Long id = extraerId(request.getPathInfo());
            Producto producto = OBJECT_MAPPER.readValue(request.getInputStream(), Producto.class);
            Producto actualizado = productoService.actualizar(id, producto);
            responderJson(response, HttpServletResponse.SC_OK, actualizado);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, "JSON inválido");
        } catch (RecursoNoEncontradoException e) {
            responderError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            responderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Long id = extraerId(request.getPathInfo());
            productoService.eliminar(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (RecursoNoEncontradoException e) {
            responderError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
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

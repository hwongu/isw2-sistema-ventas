package pe.edu.ulima.isw2.ventas.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pe.edu.ulima.isw2.ventas.client.PagoClient;
import pe.edu.ulima.isw2.ventas.dto.PagoRequest;
import pe.edu.ulima.isw2.ventas.dto.PagoResponse;
import pe.edu.ulima.isw2.ventas.exception.VentaException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class PagoClientHttpImpl implements PagoClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String PAGO_SERVICE_URL = cargarUrlPago();
    private final HttpClient httpClient;

    public PagoClientHttpImpl() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public PagoResponse procesarPago(PagoRequest request) {
        try {
            String requestJson = OBJECT_MAPPER.writeValueAsString(request);

            System.out.println("======================================");
            System.out.println("REQUEST VENTAS -> PAGOS");
            System.out.println("URL: " + PAGO_SERVICE_URL);
            System.out.println("JSON: " + requestJson);
            System.out.println("======================================");

            HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(PAGO_SERVICE_URL)).timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestJson)).build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("======================================");
            System.out.println("RESPONSE PAGOS -> VENTAS");
            System.out.println("HTTP: " + response.statusCode());
            System.out.println("BODY: " + response.body());
            System.out.println("======================================");

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return OBJECT_MAPPER.readValue(response.body(), PagoResponse.class);
            }

            throw new VentaException("El servicio de pagos respondió con estado HTTP: " + statusCode + ". Respuesta: " + response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VentaException("No se pudo procesar el pago con pagos-service", e);
        } catch (IOException e) {
            throw new VentaException("No se pudo procesar el pago con pagos-service", e);
        }
    }

    private static String cargarUrlPago() {
        Properties properties = new Properties();
        try (InputStream inputStream = PagoClientHttpImpl.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontró el archivo application.properties");
            }
            properties.load(inputStream);
            String url = properties.getProperty("pago.service.url");
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("No se configuró pago.service.url en application.properties");
            }
            return url;
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error al cargar application.properties: " + e.getMessage());
        }
    }
}

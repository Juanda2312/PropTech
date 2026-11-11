package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.service.ChatbotProxyService;
import co.edu.uniquindio.poo.PropTech.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotProxyService proxy;

    public ChatbotController(ChatbotProxyService proxy) {
        this.proxy = proxy;
    }

    // ----------------------------------------------------------------
    // POST /api/chatbot/completions
    // Proxy hacia OpenRouter: 200 con el JSON del proveedor,
    // 503 si no hay clave configurada, 502 si el proveedor falla.
    // ----------------------------------------------------------------
    @PostMapping("/completions")
    public ResponseEntity<?> completions(@RequestBody Map<String, Object> cuerpo,
                                         HttpServletRequest request) {
        if (!proxy.estaConfigurado()) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "PropBot no está configurado", request);
        }
        try {
            String respuesta = proxy.reenviar(ChatbotProxyService.sanear(cuerpo));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(respuesta);
        } catch (RestClientException e) {
            return error(HttpStatus.BAD_GATEWAY, "No se pudo contactar al proveedor del chatbot", request);
        }
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI()));
    }
}
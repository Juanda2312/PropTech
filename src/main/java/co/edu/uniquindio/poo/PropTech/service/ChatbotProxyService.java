package co.edu.uniquindio.poo.PropTech.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reenvía las peticiones de PropBot a OpenRouter usando la clave del servidor,
 * para que nunca viaje en el bundle del frontend.
 */
@Service
public class ChatbotProxyService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODELO = "openrouter/free";
    private static final int MAX_TOKENS = 2048;

    private final String apiKey;
    private final RestClient restClient = RestClient.create();

    public ChatbotProxyService(@Value("${proptech.openrouter.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String reenviar(Map<String, Object> cuerpo) {
        return restClient.post()
                .uri(OPENROUTER_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-Title", "PropTech Chatbot")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(String.class);
    }

    /** Evita abusar la clave: fuerza el modelo y limita max_tokens. */
    public static Map<String, Object> sanear(Map<String, Object> cuerpo) {
        Map<String, Object> copia = new LinkedHashMap<>(cuerpo);
        copia.put("model", MODELO);
        int pedido = copia.get("max_tokens") instanceof Number n ? n.intValue() : MAX_TOKENS;
        copia.put("max_tokens", Math.max(1, Math.min(pedido, MAX_TOKENS)));
        return copia;
    }
}
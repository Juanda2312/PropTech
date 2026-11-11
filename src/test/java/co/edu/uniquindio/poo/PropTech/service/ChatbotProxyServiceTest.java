package co.edu.uniquindio.poo.PropTech.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatbotProxyServiceTest {

    @Test
    void sanear_fuerzaModeloYLimitaMaxTokens() {
        Map<String, Object> in = Map.of("model", "gpt-4", "max_tokens", 99999, "messages", List.of());

        Map<String, Object> out = ChatbotProxyService.sanear(in);

        assertEquals("openrouter/free", out.get("model"));
        assertEquals(2048, out.get("max_tokens"));
        assertEquals(List.of(), out.get("messages"));
    }
}
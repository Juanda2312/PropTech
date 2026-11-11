package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.service.ChatbotProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatbotController.class)
class ChatbotControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ChatbotProxyService proxy;

    @Test
    void sinClaveConfigurada_responde503() throws Exception {
        when(proxy.estaConfigurado()).thenReturn(false);

        mvc.perform(post("/api/chatbot/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[]}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void conClave_devuelveLaRespuestaDelProveedor() throws Exception {
        when(proxy.estaConfigurado()).thenReturn(true);
        when(proxy.reenviar(any())).thenReturn("{\"choices\":[]}");

        mvc.perform(post("/api/chatbot/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[]}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"choices\":[]}"));
    }

    @Test
    void errorDelProveedor_responde502() throws Exception {
        when(proxy.estaConfigurado()).thenReturn(true);
        when(proxy.reenviar(any())).thenThrow(new RestClientException("fallo"));

        mvc.perform(post("/api/chatbot/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[]}"))
                .andExpect(status().isBadGateway());
    }
}
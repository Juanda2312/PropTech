package co.edu.uniquindio.poo.PropTech.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "proptech.cors.allowed-origins=https://proptech.vercel.app")
@AutoConfigureMockMvc
class WebConfigCorsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void preflightPATCH_desdeOrigenPermitido_respondeConEseOrigen() throws Exception {
        mvc.perform(options("/api/visitas/V-1/confirmar")
                        .header("Origin", "https://proptech.vercel.app")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://proptech.vercel.app"));
    }

    @Test
    void preflight_desdeOrigenNoPermitido_seRechazaConForbidden() throws Exception {
        mvc.perform(options("/api/visitas/V-1/confirmar")
                        .header("Origin", "https://otro-sitio.example")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isForbidden());
    }
}
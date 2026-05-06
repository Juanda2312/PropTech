package co.edu.uniquindio.poo.PropTech.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de Jackson (serialización/deserialización JSON).
 *
 * El proyecto usa LocalDate y LocalTime en varios DTOs y entidades
 * (VisitaDTO, Operacion, Alerta, etc.). Sin esta configuración,
 * Jackson serializa esas fechas como arrays numéricos [2025, 5, 6]
 * en lugar del formato ISO "2025-05-06", lo que rompe el contrato
 * de la API REST.
 *
 * Registra JavaTimeModule y desactiva WRITE_DATES_AS_TIMESTAMPS
 * para obtener strings ISO-8601 en todas las respuestas.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

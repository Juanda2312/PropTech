package co.edu.uniquindio.poo.PropTech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS para la API REST.
 *
 * Permite que cualquier cliente frontend (React, Angular, etc.)
 * consuma los endpoints desde el mismo equipo o desde un servidor
 * de desarrollo externo sin bloqueos del navegador.
 *
 * En producción reemplaza allowedOrigins("*") por el dominio real
 * del frontend para mayor seguridad.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")          // en prod: "https://tu-frontend.com"
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

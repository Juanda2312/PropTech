package co.edu.uniquindio.poo.PropTech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuración global de CORS para la API REST.
 *
 * Solo los orígenes listados en la propiedad proptech.cors.allowed-origins
 * (variable de entorno CORS_ALLOWED_ORIGINS, separados por comas) pueden
 * consumir /api/** desde un navegador. Admite comodines, por ejemplo
 * "https://proptech-*.vercel.app" para los despliegues de preview.
 *
 * Por defecto solo se permite http://localhost:4200 (ng serve).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${proptech.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins)
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
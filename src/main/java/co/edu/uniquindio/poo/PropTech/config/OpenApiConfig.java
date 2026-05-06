package co.edu.uniquindio.poo.PropTech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI 3 (Swagger UI).
 *
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI propTechOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PropTech API")
                        .description("""
                                API REST de la plataforma inmobiliaria PropTech.
                                Gestiona inmuebles, clientes, asesores, visitas,
                                operaciones de negocio, alertas automáticas y
                                recomendaciones inteligentes.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Universidad del Quindío — POO")
                                .email("poo@uniquindio.edu.co"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

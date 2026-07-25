package com.jodak.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI / Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI olympicsOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Plateforme JO — API REST")
                .description("""
                        API REST de gestion des Jeux Olympiques : disciplines, athlètes, épreuves, \
                        résultats, tableau des médailles et statistiques. \
                        Un Web Service SOAP en lecture seule est destiné au système d'information historique.""")
                .version("v1")
                .contact(new Contact().name("Équipe Plateforme JO"))
                .license(new License().name("Usage pédagogique")));
    }
}

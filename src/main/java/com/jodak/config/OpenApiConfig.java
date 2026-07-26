package com.jodak.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI / Swagger UI.
 *
 * <p>Un schéma de sécurité « Bearer JWT » est déclaré : le bouton <b>Authorize</b> de Swagger UI
 * permet de saisir un access token pour appeler les endpoints protégés (mutations et
 * administration). Les endpoints publics de lecture restent appelables sans jeton.</p>
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI olympicsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plateforme JO — API REST")
                        .description("""
                                API REST de gestion des Jeux Olympiques : disciplines, athlètes, épreuves, \
                                résultats, tableau des médailles et statistiques. \
                                Un Web Service SOAP en lecture seule est destiné au système d'information historique.""")
                        .version("v1")
                        .contact(new Contact().name("Équipe Plateforme JO"))
                        .license(new License().name("Usage pédagogique")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token JWT (obtenu via POST /api/admin/auth/login).")));
    }
}

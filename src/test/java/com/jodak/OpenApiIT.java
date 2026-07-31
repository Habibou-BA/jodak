package com.jodak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que la documentation OpenAPI est bien générée (Swagger exploitable). Ce test protège
 * contre une incompatibilité entre springdoc et la version de Spring Boot : la génération de
 * {@code /v3/api-docs} échouerait (HTTP 500) sans que les tests fonctionnels ne s'en aperçoivent.
 */
@AutoConfigureMockMvc
class OpenApiIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La spécification OpenAPI /v3/api-docs est générée")
    void apiDocsIsGenerated() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/v1/disciplines']").exists());
    }
}

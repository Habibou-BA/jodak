package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.AbstractIntegrationTest;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.repositories.DisciplineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de bout en bout de la gestion des disciplines (HTTP → JPA → PostgreSQL).
 * Vérifie notamment la persistance réelle et le renseignement des champs d'audit.
 */
@AutoConfigureMockMvc
class DisciplineIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DisciplineRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST persiste la discipline avec ses champs d'audit puis GET la restitue")
    void createPersistsAndAuditFieldsAreSet() throws Exception {
        String location = mockMvc.perform(post("/api/v1/disciplines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("Athlétisme"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Athlétisme"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn().getResponse().getHeader("Location");

        assertThat(repository.count()).isEqualTo(1);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Athlétisme"));
    }

    @Test
    @DisplayName("POST d'un nom déjà utilisé (casse différente) renvoie 409")
    void createDuplicateNameReturns409() throws Exception {
        mockMvc.perform(post("/api/v1/disciplines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("Judo"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/disciplines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("JUDO"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflit"));
    }
}

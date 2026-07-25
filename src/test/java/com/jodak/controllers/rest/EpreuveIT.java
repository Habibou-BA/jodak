package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.AbstractIntegrationTest;
import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.entities.Discipline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de bout en bout de la gestion des épreuves (HTTP → JPA → PostgreSQL),
 * incluant l'unicité (libellé, discipline, date) et la recherche par discipline / par date.
 */
@AutoConfigureMockMvc
class EpreuveIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Long disciplineId;

    @BeforeEach
    void setUp() {
        // Nettoyage assuré par AbstractIntegrationTest#resetDatabase.
        disciplineId = disciplineRepository.save(Discipline.builder().name("Athlétisme").build()).getId();
    }

    private EpreuveRequest request(String label, LocalDate date) {
        return new EpreuveRequest(label, disciplineId, date);
    }

    private void create(String label, LocalDate date) throws Exception {
        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(label, date))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST persiste l'épreuve et restitue le résumé de la discipline")
    void createPersists() throws Exception {
        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("100 m", LocalDate.of(2024, 8, 4)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("100 m"))
                .andExpect(jsonPath("$.discipline.name").value("Athlétisme"))
                .andExpect(jsonPath("$.eventDate").value("2024-08-04"));
    }

    @Test
    @DisplayName("POST d'un triplet déjà existant (casse différente) renvoie 409")
    void createDuplicateReturns409() throws Exception {
        create("100 m", LocalDate.of(2024, 8, 4));

        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("100 M", LocalDate.of(2024, 8, 4)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflit"));
    }

    @Test
    @DisplayName("POST référençant une discipline inexistante renvoie 404")
    void createUnknownDisciplineReturns404() throws Exception {
        EpreuveRequest invalid = new EpreuveRequest("200 m", 999999L, LocalDate.of(2024, 8, 5));

        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("La recherche filtre par discipline et par plage de dates")
    void searchFiltersByDisciplineAndDate() throws Exception {
        create("100 m", LocalDate.of(2024, 8, 4));
        create("200 m", LocalDate.of(2024, 8, 6));

        mockMvc.perform(get("/api/v1/epreuves").param("disciplineId", disciplineId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/epreuves")
                        .param("eventDateFrom", "2024-08-05")
                        .param("eventDateTo", "2024-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].label").value("200 m"));
    }
}

package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.AbstractIntegrationTest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.enums.Gender;
import com.jodak.repositories.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de bout en bout de la gestion des athlètes (HTTP → JPA → PostgreSQL),
 * incluant la recherche multicritère par Specifications et la sous-ressource discipline.
 */
@AutoConfigureMockMvc
class AthleteIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountryRepository countryRepository;

    private Long disciplineId;
    private Long countryId;

    @BeforeEach
    void setUp() {
        // Le nettoyage des tables est assuré par AbstractIntegrationTest#resetDatabase.
        disciplineId = disciplineRepository.save(Discipline.builder().name("Athlétisme").build()).getId();
        Country country = countryRepository.findAll().get(0); // nation issue du jeu de référence (V2)
        countryId = country.getId();
    }

    private AthleteRequest request(String lastName, Gender gender) {
        return new AthleteRequest(lastName, "Usain", gender,
                LocalDate.of(1990, 5, 12), countryId, disciplineId, 190, 88);
    }

    private void create(String lastName, Gender gender) throws Exception {
        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(lastName, gender))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST persiste l'athlète et restitue les résumés imbriqués + l'âge")
    void createPersistsWithNestedSummaries() throws Exception {
        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Bolt", Gender.MALE))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastName").value("Bolt"))
                .andExpect(jsonPath("$.discipline.name").value("Athlétisme"))
                .andExpect(jsonPath("$.country.code").exists())
                .andExpect(jsonPath("$.age").isNumber());

        assertThat(athleteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST référençant une nationalité inexistante renvoie 404")
    void createUnknownCountryReturns404() throws Exception {
        AthleteRequest invalid = new AthleteRequest("Bolt", "Usain", Gender.MALE,
                LocalDate.of(1990, 5, 12), 999999L, disciplineId, 190, 88);

        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"));
    }

    @Test
    @DisplayName("La recherche filtre par sexe et pagine")
    void searchFiltersByGender() throws Exception {
        create("Bolt", Gender.MALE);
        create("Fraser", Gender.FEMALE);

        mockMvc.perform(get("/api/v1/athletes")
                        .param("gender", "FEMALE")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Fraser"));
    }

    @Test
    @DisplayName("La sous-ressource /disciplines/{id}/athletes liste les athlètes de la discipline")
    void disciplineSubResourceListsAthletes() throws Exception {
        create("Bolt", Gender.MALE);

        mockMvc.perform(get("/api/v1/disciplines/{id}/athletes", disciplineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Bolt"));
    }

    @Test
    @DisplayName("PATCH modifie partiellement le poids")
    void patchUpdatesWeight() throws Exception {
        String body = mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Bolt", Gender.MALE))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(patch("/api/v1/athletes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weightKg\": 80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(80))
                .andExpect(jsonPath("$.lastName").value("Bolt"));
    }
}

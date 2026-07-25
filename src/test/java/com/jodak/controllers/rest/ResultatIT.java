package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.AbstractIntegrationTest;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.enums.Gender;
import com.jodak.repositories.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de bout en bout de la gestion des résultats : attribution automatique des
 * médailles (RM-12), podium (UC-09), unicités (RM-10/11) et cohérence discipline (RM-09).
 */
@AutoConfigureMockMvc
class ResultatIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountryRepository countryRepository;

    private Long epreuveId;
    private Long athlete1Id;
    private Long athlete2Id;
    private Long athlete3Id;
    private Long athleteOtherDisciplineId;

    @BeforeEach
    void setUp() {
        Discipline athletisme = disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        Discipline natation = disciplineRepository.save(Discipline.builder().name("Natation").build());
        Country country = countryRepository.findAll().get(0);

        epreuveId = epreuveRepository.save(Epreuve.builder()
                .label("100 m").discipline(athletisme).eventDate(LocalDate.of(2024, 8, 4)).build()).getId();

        athlete1Id = saveAthlete("Bolt", athletisme, country);
        athlete2Id = saveAthlete("Blake", athletisme, country);
        athlete3Id = saveAthlete("Gatlin", athletisme, country);
        athleteOtherDisciplineId = saveAthlete("Phelps", natation, country);
    }

    private Long saveAthlete(String lastName, Discipline discipline, Country country) {
        return athleteRepository.save(Athlete.builder()
                .lastName(lastName).firstName("X").gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1)).country(country).discipline(discipline)
                .heightCm(185).weightKg(80).build()).getId();
    }

    private ResultActions postResult(Long athleteId, int rank) throws Exception {
        return mockMvc.perform(post("/api/v1/resultats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ResultatRequest(epreuveId, athleteId, rank))));
    }

    @Test
    @DisplayName("POST attribue automatiquement or/argent/bronze selon le rang")
    void createAssignsMedalsByRank() throws Exception {
        postResult(athlete1Id, 1).andExpect(status().isCreated()).andExpect(jsonPath("$.medal").value("OR"));
        postResult(athlete2Id, 2).andExpect(status().isCreated()).andExpect(jsonPath("$.medal").value("ARGENT"));
        postResult(athlete3Id, 3).andExpect(status().isCreated()).andExpect(jsonPath("$.medal").value("BRONZE"));
    }

    @Test
    @DisplayName("Le rang 4 ne donne aucune médaille (RM-12)")
    void rankFourHasNoMedal() throws Exception {
        postResult(athlete1Id, 4)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank").value(4))
                .andExpect(jsonPath("$.medal").value(nullValue()));
    }

    @Test
    @DisplayName("Le podium liste les médaillés ordonnés par rang")
    void podiumReturnsOrderedMedalists() throws Exception {
        postResult(athlete1Id, 1);
        postResult(athlete2Id, 2);
        postResult(athlete3Id, 3);

        mockMvc.perform(get("/api/v1/epreuves/{id}/podium", epreuveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions.length()").value(3))
                .andExpect(jsonPath("$.positions[0].medal").value("OR"))
                .andExpect(jsonPath("$.positions[0].athlete.lastName").value("Bolt"))
                .andExpect(jsonPath("$.positions[2].medal").value("BRONZE"));
    }

    @Test
    @DisplayName("Un rang déjà attribué pour l'épreuve renvoie 409")
    void duplicateRankReturns409() throws Exception {
        postResult(athlete1Id, 1).andExpect(status().isCreated());
        postResult(athlete2Id, 1).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un athlète déjà classé sur l'épreuve renvoie 409")
    void duplicateAthleteReturns409() throws Exception {
        postResult(athlete1Id, 1).andExpect(status().isCreated());
        postResult(athlete1Id, 2).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un athlète d'une autre discipline renvoie 422")
    void disciplineMismatchReturns422() throws Exception {
        postResult(athleteOtherDisciplineId, 1)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Règle métier non respectée"));
    }
}

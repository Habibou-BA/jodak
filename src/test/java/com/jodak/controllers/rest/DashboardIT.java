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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration du tableau de bord : compteurs globaux et classement par points.
 */
@AutoConfigureMockMvc
class DashboardIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountryRepository countryRepository;

    private Country countryB;

    @BeforeEach
    void setUp() throws Exception {
        Discipline athletisme = disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        Country countryA = countryRepository.findAll().get(0);
        countryB = countryRepository.findAll().get(1);

        Long epreuve1 = saveEpreuve("100 m", athletisme, LocalDate.of(2024, 8, 4));
        Long epreuve2 = saveEpreuve("200 m", athletisme, LocalDate.of(2024, 8, 6));

        Long aA = saveAthlete("Alpha", athletisme, countryA);
        Long aB = saveAthlete("Bravo", athletisme, countryB);
        Long aB2 = saveAthlete("Charlie", athletisme, countryB);

        postResult(epreuve1, aA, 1);   // A : or
        postResult(epreuve1, aB, 2);   // B : argent
        postResult(epreuve2, aB2, 1);  // B : or
    }

    private Long saveEpreuve(String label, Discipline discipline, LocalDate date) {
        return epreuveRepository.save(
                Epreuve.builder().label(label).discipline(discipline).eventDate(date).build()).getId();
    }

    private Long saveAthlete(String lastName, Discipline discipline, Country country) {
        return athleteRepository.save(Athlete.builder()
                .lastName(lastName).firstName("X").gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1)).country(country).discipline(discipline)
                .heightCm(185).weightKg(80).build()).getId();
    }

    private void postResult(Long epreuveId, Long athleteId, int rank) throws Exception {
        mockMvc.perform(post("/api/v1/resultats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResultatRequest(epreuveId, athleteId, rank))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Le tableau de bord agrège les compteurs et classe par points")
    void dashboardAggregatesAndRanks() throws Exception {
        mockMvc.perform(get("/api/v1/tableau-de-bord"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAthletes").value(3))
                .andExpect(jsonPath("$.totalCountries").value(2))
                .andExpect(jsonPath("$.totalDisciplines").value(1))
                .andExpect(jsonPath("$.totalEpreuves").value(2))
                .andExpect(jsonPath("$.totalGold").value(2))
                .andExpect(jsonPath("$.totalSilver").value(1))
                .andExpect(jsonPath("$.totalBronze").value(0))
                .andExpect(jsonPath("$.totalMedals").value(3))
                .andExpect(jsonPath("$.pointsRanking[0].country.code").value(countryB.getCode()))
                .andExpect(jsonPath("$.pointsRanking[0].points").value(11));
    }
}

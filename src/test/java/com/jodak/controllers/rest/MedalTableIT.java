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
 * Test d'intégration du tableau des médailles : agrégation par nation et classement officiel.
 */
@AutoConfigureMockMvc
class MedalTableIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountryRepository countryRepository;

    private Country countryA;
    private Country countryB;

    @BeforeEach
    void setUp() throws Exception {
        Discipline athletisme = disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        countryA = countryRepository.findAll().get(0);
        countryB = countryRepository.findAll().get(1);

        Long epreuve1 = saveEpreuve("100 m", athletisme, LocalDate.of(2024, 8, 4));
        Long epreuve2 = saveEpreuve("200 m", athletisme, LocalDate.of(2024, 8, 6));

        Long aA = saveAthlete("Alpha", athletisme, countryA);
        Long aB = saveAthlete("Bravo", athletisme, countryB);
        Long aB2 = saveAthlete("Charlie", athletisme, countryB);

        // Nation A : 1 or. Nation B : 1 or + 1 argent.
        postResult(epreuve1, aA, 1);
        postResult(epreuve1, aB, 2);
        postResult(epreuve2, aB2, 1);
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
    @DisplayName("Le tableau classe la nation ayant plus d'argent devant à or égal")
    void medalTableRanksBySilverWhenGoldTied() throws Exception {
        mockMvc.perform(get("/api/v1/tableau-medailles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].country.code").value(countryB.getCode()))
                .andExpect(jsonPath("$[0].gold").value(1))
                .andExpect(jsonPath("$[0].silver").value(1))
                .andExpect(jsonPath("$[0].total").value(2))
                .andExpect(jsonPath("$[1].rank").value(2))
                .andExpect(jsonPath("$[1].country.code").value(countryA.getCode()))
                .andExpect(jsonPath("$[1].total").value(1));
    }
}

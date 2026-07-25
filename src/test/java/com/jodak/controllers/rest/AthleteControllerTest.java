package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.enums.Gender;
import com.jodak.services.interfaces.AthleteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AthleteController.class)
class AthleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AthleteService service;

    private AthleteRequest valid() {
        return new AthleteRequest("Bolt", "Usain", Gender.MALE,
                LocalDate.of(1986, 8, 21), 3L, 1L, 195, 94);
    }

    @Test
    @DisplayName("POST valide renvoie 201 avec l'en-tête Location")
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(new AthleteResponse(
                7L, "Bolt", "Usain", Gender.MALE, LocalDate.of(1986, 8, 21), 39,
                null, null, 195, 94, null, null));

        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/athletes/7")))
                .andExpect(jsonPath("$.lastName").value("Bolt"));
    }

    @Test
    @DisplayName("POST invalide (nationalité manquante) renvoie 400 avec le détail")
    void createInvalidReturns400() throws Exception {
        AthleteRequest invalid = new AthleteRequest("Bolt", "Usain", Gender.MALE,
                LocalDate.of(1986, 8, 21), null, 1L, 195, 94);

        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.countryId").exists());
    }

    @Test
    @DisplayName("POST avec taille hors bornes renvoie 400")
    void createOutOfBoundsReturns400() throws Exception {
        AthleteRequest invalid = new AthleteRequest("Bolt", "Usain", Gender.MALE,
                LocalDate.of(1986, 8, 21), 3L, 1L, 50, 94);

        mockMvc.perform(post("/api/v1/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.heightCm").exists());
    }
}

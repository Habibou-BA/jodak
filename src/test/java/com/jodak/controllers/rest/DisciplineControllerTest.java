package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.services.interfaces.DisciplineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisciplineController.class)
class DisciplineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DisciplineService service;

    @Test
    @DisplayName("POST valide renvoie 201 avec l'en-tête Location")
    void createReturns201() throws Exception {
        when(service.create(any()))
                .thenReturn(new DisciplineResponse(1L, "Athlétisme", null, null));

        mockMvc.perform(post("/api/v1/disciplines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("Athlétisme"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/disciplines/1")))
                .andExpect(jsonPath("$.name").value("Athlétisme"));
    }

    @Test
    @DisplayName("POST avec nom vide renvoie 400 et le détail des erreurs")
    void createBlankReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/disciplines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("GET introuvable renvoie 404 au format ProblemDetail")
    void getByIdNotFoundReturns404() throws Exception {
        when(service.getById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Discipline introuvable pour l'identifiant 99."));

        mockMvc.perform(get("/api/v1/disciplines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.detail").value("Discipline introuvable pour l'identifiant 99."));
    }
}

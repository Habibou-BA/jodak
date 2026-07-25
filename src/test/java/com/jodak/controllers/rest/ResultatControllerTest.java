package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.enums.Medal;
import com.jodak.services.interfaces.ResultatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResultatController.class)
class ResultatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResultatService service;

    @Test
    @DisplayName("POST valide renvoie 201 avec l'en-tête Location et la médaille")
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(new ResultatResponse(
                6L, null, null, 1, Medal.OR, null, null));

        mockMvc.perform(post("/api/v1/resultats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResultatRequest(1L, 1L, 1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/resultats/6")))
                .andExpect(jsonPath("$.medal").value("OR"));
    }

    @Test
    @DisplayName("POST sans rang renvoie 400 avec le détail")
    void createMissingRankReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/resultats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResultatRequest(1L, 1L, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rank").exists());
    }
}

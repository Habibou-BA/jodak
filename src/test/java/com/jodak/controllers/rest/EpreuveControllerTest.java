package com.jodak.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.services.interfaces.EpreuveService;
import com.jodak.services.interfaces.ResultatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.jodak.admin.security.SecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(EpreuveController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class EpreuveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EpreuveService service;

    @MockBean
    private ResultatService resultatService;

    @Test
    @DisplayName("POST valide renvoie 201 avec l'en-tête Location")
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(new EpreuveResponse(
                4L, "100 m", null, LocalDate.of(2024, 8, 4), null, null));

        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EpreuveRequest("100 m", 1L, LocalDate.of(2024, 8, 4)))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/epreuves/4")))
                .andExpect(jsonPath("$.label").value("100 m"));
    }

    @Test
    @DisplayName("POST sans libellé renvoie 400 avec le détail")
    void createBlankLabelReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/epreuves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EpreuveRequest("  ", 1L, LocalDate.of(2024, 8, 4)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.label").exists());
    }
}

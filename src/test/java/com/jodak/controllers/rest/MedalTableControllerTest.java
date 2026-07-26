package com.jodak.controllers.rest;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.services.interfaces.MedalTableService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.jodak.admin.security.SecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedalTableController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class MedalTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedalTableService service;

    @Test
    @DisplayName("GET renvoie le tableau des médailles")
    void getReturnsMedalTable() throws Exception {
        when(service.getMedalTable()).thenReturn(List.of(
                new MedalTableRowResponse(1, new CountrySummary(1L, "USA", "États-Unis"), 12, 8, 5, 25)));

        mockMvc.perform(get("/api/v1/tableau-medailles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].country.code").value("USA"))
                .andExpect(jsonPath("$[0].total").value(25));
    }
}

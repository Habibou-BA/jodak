package com.jodak.controllers.rest;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.dashboard.DashboardResponse;
import com.jodak.dtos.dashboard.PointsRankingRow;
import com.jodak.services.interfaces.DashboardService;
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

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService service;

    @Test
    @DisplayName("GET renvoie la synthèse du tableau de bord")
    void getReturnsDashboard() throws Exception {
        when(service.getDashboard()).thenReturn(new DashboardResponse(
                3, 2, 1, 2, 2, 1, 0, 3,
                List.of(new PointsRankingRow(1, new CountrySummary(2L, "BBB", "Beta"), 11, 1, 1, 0, 2))));

        mockMvc.perform(get("/api/v1/tableau-de-bord"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAthletes").value(3))
                .andExpect(jsonPath("$.totalMedals").value(3))
                .andExpect(jsonPath("$.pointsRanking[0].points").value(11));
    }

    @Test
    @DisplayName("GET /classement-points renvoie le classement par points")
    void getReturnsPointsRanking() throws Exception {
        when(service.getPointsRanking()).thenReturn(
                List.of(new PointsRankingRow(1, new CountrySummary(2L, "BBB", "Beta"), 11, 1, 1, 0, 2)));

        mockMvc.perform(get("/api/v1/tableau-de-bord/classement-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country.code").value("BBB"))
                .andExpect(jsonPath("$[0].points").value(11));
    }
}

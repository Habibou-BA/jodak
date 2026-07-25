package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.dashboard.DashboardResponse;
import com.jodak.dtos.dashboard.PointsRankingRow;
import com.jodak.services.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Points d'entrée REST du tableau de bord (statistiques).
 */
@RestController
@RequestMapping(ApiPaths.DASHBOARD)
@RequiredArgsConstructor
@Tag(name = "Tableau de bord", description = "Statistiques de la plateforme")
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    @Operation(summary = "Consulter la synthèse statistique de la plateforme")
    public DashboardResponse getDashboard() {
        return service.getDashboard();
    }

    @GetMapping("/classement-points")
    @Operation(summary = "Consulter le classement des nations par points (Or=7, Argent=4, Bronze=1)")
    public List<PointsRankingRow> getPointsRanking() {
        return service.getPointsRanking();
    }
}

package com.jodak.dtos.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Synthèse statistique de la plateforme (tableau de bord).
 */
@Schema(description = "Statistiques du tableau de bord")
public record DashboardResponse(
        @Schema(description = "Nombre total d'athlètes") long totalAthletes,
        @Schema(description = "Nombre de pays participants (ayant au moins un athlète)") long totalCountries,
        @Schema(description = "Nombre de disciplines") long totalDisciplines,
        @Schema(description = "Nombre d'épreuves") long totalEpreuves,
        @Schema(description = "Nombre de médailles d'or") long totalGold,
        @Schema(description = "Nombre de médailles d'argent") long totalSilver,
        @Schema(description = "Nombre de médailles de bronze") long totalBronze,
        @Schema(description = "Nombre total de médailles") long totalMedals,
        @Schema(description = "Classement des nations par points (Or = 7, Argent = 4, Bronze = 1)")
        List<PointsRankingRow> pointsRanking
) {
}

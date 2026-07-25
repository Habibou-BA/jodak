package com.jodak.dtos.dashboard;

import com.jodak.dtos.country.CountrySummary;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Une ligne du classement des nations par points (Or = 7, Argent = 4, Bronze = 1).
 */
@Schema(description = "Ligne du classement par points")
public record PointsRankingRow(
        @Schema(description = "Rang au classement", example = "1") int rank,
        CountrySummary country,
        @Schema(description = "Total de points", example = "116") long points,
        @Schema(example = "12") long gold,
        @Schema(example = "8") long silver,
        @Schema(example = "5") long bronze,
        @Schema(description = "Nombre total de médailles", example = "25") long total
) {
}

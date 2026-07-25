package com.jodak.dtos.medaltable;

import com.jodak.dtos.country.CountrySummary;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Une ligne du tableau officiel des médailles.
 */
@Schema(description = "Ligne du tableau des médailles")
public record MedalTableRowResponse(
        @Schema(description = "Rang au classement", example = "1") int rank,
        CountrySummary country,
        @Schema(example = "12") long gold,
        @Schema(example = "8") long silver,
        @Schema(example = "5") long bronze,
        @Schema(description = "Nombre total de médailles", example = "25") long total
) {
}

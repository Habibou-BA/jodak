package com.jodak.dtos.resultat;

import com.jodak.dtos.athlete.AthleteSummary;
import com.jodak.dtos.epreuve.EpreuveSummary;
import com.jodak.enums.Medal;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Représentation d'un résultat renvoyée par l'API.
 */
@Schema(description = "Résultat")
public record ResultatResponse(
        Long id,
        EpreuveSummary epreuve,
        AthleteSummary athlete,
        @Schema(description = "Rang de l'athlète", example = "1") Integer rank,
        @Schema(description = "Médaille dérivée du rang (null au-delà du 3e)") Medal medal,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

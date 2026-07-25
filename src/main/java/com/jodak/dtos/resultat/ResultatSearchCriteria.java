package com.jodak.dtos.resultat;

import com.jodak.enums.Medal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Critères de recherche des résultats. Tous les champs sont facultatifs.
 */
@Schema(description = "Critères de recherche des résultats (tous facultatifs)")
public record ResultatSearchCriteria(

        @Schema(description = "Identifiant de l'épreuve") Long epreuveId,

        @Schema(description = "Identifiant de l'athlète") Long athleteId,

        @Schema(description = "Médaille") Medal medal
) {
}

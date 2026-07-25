package com.jodak.dtos.epreuve;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Critères de recherche multicritère des épreuves. Tous les champs sont facultatifs.
 */
@Schema(description = "Critères de recherche des épreuves (tous facultatifs)")
public record EpreuveSearchCriteria(

        @Schema(description = "Filtre sur le libellé (contient, insensible à la casse)")
        String label,

        @Schema(description = "Identifiant de la discipline") Long disciplineId,

        @Schema(description = "Date exacte de l'épreuve") LocalDate eventDate,

        @Schema(description = "Épreuves à partir de cette date (incluse)") LocalDate eventDateFrom,

        @Schema(description = "Épreuves jusqu'à cette date (incluse)") LocalDate eventDateTo
) {
}

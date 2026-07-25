package com.jodak.dtos.athlete;

import com.jodak.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Critères de recherche multicritère des athlètes. Tous les champs sont facultatifs.
 */
@Schema(description = "Critères de recherche des athlètes (tous facultatifs)")
public record AthleteSearchCriteria(

        @Schema(description = "Filtre sur le nom (contient, insensible à la casse)")
        String lastName,

        @Schema(description = "Filtre sur le prénom (contient, insensible à la casse)")
        String firstName,

        @Schema(description = "Sexe") Gender gender,

        @Schema(description = "Identifiant de la nationalité") Long countryId,

        @Schema(description = "Identifiant de la discipline") Long disciplineId,

        @Schema(description = "Né(e) à partir de cette date (incluse)") LocalDate birthDateFrom,

        @Schema(description = "Né(e) jusqu'à cette date (incluse)") LocalDate birthDateTo
) {
}

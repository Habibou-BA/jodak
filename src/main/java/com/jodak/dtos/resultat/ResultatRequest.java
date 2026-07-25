package com.jodak.dtos.resultat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Données d'enregistrement (POST) ou de correction (PUT) d'un résultat. La médaille n'est jamais
 * fournie : elle est dérivée automatiquement du rang (RM-12).
 */
@Schema(description = "Requête d'enregistrement d'un résultat")
public record ResultatRequest(

        @Schema(description = "Identifiant de l'épreuve", example = "1")
        @NotNull(message = "L'épreuve est obligatoire.")
        @Positive(message = "L'identifiant de l'épreuve doit être positif.")
        Long epreuveId,

        @Schema(description = "Identifiant de l'athlète", example = "1")
        @NotNull(message = "L'athlète est obligatoire.")
        @Positive(message = "L'identifiant de l'athlète doit être positif.")
        Long athleteId,

        @Schema(description = "Rang de l'athlète dans l'épreuve (1 = premier)", example = "1")
        @NotNull(message = "Le rang est obligatoire.")
        @Positive(message = "Le rang doit être un entier positif.")
        Integer rank
) {
}

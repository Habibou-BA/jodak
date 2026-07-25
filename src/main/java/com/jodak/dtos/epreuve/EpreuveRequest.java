package com.jodak.dtos.epreuve;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données de création (POST) ou de modification complète (PUT) d'une épreuve.
 */
@Schema(description = "Requête de création ou de modification d'une épreuve")
public record EpreuveRequest(

        @Schema(example = "100 m")
        @NotBlank(message = "Le libellé de l'épreuve est obligatoire.")
        @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères.")
        String label,

        @Schema(description = "Identifiant de la discipline", example = "1")
        @NotNull(message = "La discipline est obligatoire.")
        @Positive(message = "L'identifiant de la discipline doit être positif.")
        Long disciplineId,

        @Schema(description = "Date de l'épreuve", example = "2024-08-04")
        @NotNull(message = "La date de l'épreuve est obligatoire.")
        LocalDate eventDate
) {
}

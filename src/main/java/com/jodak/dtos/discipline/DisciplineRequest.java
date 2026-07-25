package com.jodak.dtos.discipline;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données de création / modification d'une discipline.
 */
@Schema(description = "Requête de création ou de modification d'une discipline")
public record DisciplineRequest(

        @Schema(description = "Nom de la discipline", example = "Athlétisme")
        @NotBlank(message = "Le nom de la discipline est obligatoire.")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
        String name
) {
}

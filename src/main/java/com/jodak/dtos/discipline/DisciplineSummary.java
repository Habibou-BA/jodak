package com.jodak.dtos.discipline;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représentation résumée d'une discipline, imbriquée dans d'autres ressources (ex. athlète).
 */
@Schema(description = "Discipline (résumé)")
public record DisciplineSummary(
        @Schema(example = "1") Long id,
        @Schema(example = "Athlétisme") String name
) {
}

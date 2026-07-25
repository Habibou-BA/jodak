package com.jodak.dtos.athlete;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représentation résumée d'un athlète, imbriquée dans d'autres ressources (résultat, podium).
 */
@Schema(description = "Athlète (résumé)")
public record AthleteSummary(
        @Schema(example = "1") Long id,
        @Schema(example = "Bolt") String lastName,
        @Schema(example = "Usain") String firstName
) {
}

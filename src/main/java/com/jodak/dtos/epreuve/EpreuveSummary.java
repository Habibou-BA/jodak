package com.jodak.dtos.epreuve;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représentation résumée d'une épreuve, imbriquée dans d'autres ressources (résultat).
 */
@Schema(description = "Épreuve (résumé)")
public record EpreuveSummary(
        @Schema(example = "1") Long id,
        @Schema(example = "100 m") String label
) {
}

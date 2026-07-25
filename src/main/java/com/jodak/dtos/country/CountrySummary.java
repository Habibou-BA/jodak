package com.jodak.dtos.country;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Représentation résumée d'une nation, imbriquée dans d'autres ressources (ex. athlète).
 */
@Schema(description = "Nation (résumé)")
public record CountrySummary(
        @Schema(example = "1") Long id,
        @Schema(example = "FRA") String code,
        @Schema(example = "France") String name
) {
}

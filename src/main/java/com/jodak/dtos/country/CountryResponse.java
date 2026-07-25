package com.jodak.dtos.country;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Représentation complète d'une nation.
 */
@Schema(description = "Nation")
public record CountryResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "FRA") String code,
        @Schema(example = "France") String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

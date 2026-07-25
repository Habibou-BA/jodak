package com.jodak.dtos.discipline;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Représentation d'une discipline renvoyée par l'API.
 */
@Schema(description = "Discipline")
public record DisciplineResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Athlétisme") String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

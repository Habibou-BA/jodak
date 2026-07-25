package com.jodak.dtos.epreuve;

import com.jodak.dtos.discipline.DisciplineSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Représentation d'une épreuve renvoyée par l'API.
 */
@Schema(description = "Épreuve")
public record EpreuveResponse(
        Long id,
        String label,
        DisciplineSummary discipline,
        LocalDate eventDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

package com.jodak.dtos.athlete;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.discipline.DisciplineSummary;
import com.jodak.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Représentation d'un athlète renvoyée par l'API.
 */
@Schema(description = "Athlète")
public record AthleteResponse(
        Long id,
        String lastName,
        String firstName,
        Gender gender,
        LocalDate birthDate,
        @Schema(description = "Âge en années révolues") int age,
        CountrySummary country,
        DisciplineSummary discipline,
        Integer heightCm,
        Integer weightKg,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

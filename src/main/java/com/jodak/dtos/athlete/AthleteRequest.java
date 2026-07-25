package com.jodak.dtos.athlete;

import com.jodak.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données de création (POST) ou de modification complète (PUT) d'un athlète.
 */
@Schema(description = "Requête de création ou de modification complète d'un athlète")
public record AthleteRequest(

        @Schema(example = "Bolt")
        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
        String lastName,

        @Schema(example = "Usain")
        @NotBlank(message = "Le prénom est obligatoire.")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères.")
        String firstName,

        @Schema(example = "MALE")
        @NotNull(message = "Le sexe est obligatoire.")
        Gender gender,

        @Schema(example = "1986-08-21")
        @NotNull(message = "La date de naissance est obligatoire.")
        @Past(message = "La date de naissance doit être dans le passé.")
        LocalDate birthDate,

        @Schema(description = "Identifiant de la nationalité", example = "17")
        @NotNull(message = "La nationalité est obligatoire.")
        @Positive(message = "L'identifiant de la nationalité doit être positif.")
        Long countryId,

        @Schema(description = "Identifiant de la discipline", example = "1")
        @NotNull(message = "La discipline est obligatoire.")
        @Positive(message = "L'identifiant de la discipline doit être positif.")
        Long disciplineId,

        @Schema(example = "195")
        @NotNull(message = "La taille est obligatoire.")
        @Min(value = 100, message = "La taille doit être comprise entre 100 et 260 cm.")
        @Max(value = 260, message = "La taille doit être comprise entre 100 et 260 cm.")
        Integer heightCm,

        @Schema(example = "94")
        @NotNull(message = "Le poids est obligatoire.")
        @Min(value = 30, message = "Le poids doit être compris entre 30 et 250 kg.")
        @Max(value = 250, message = "Le poids doit être compris entre 30 et 250 kg.")
        Integer weightKg
) {
}

package com.jodak.dtos.athlete;

import com.jodak.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données de modification partielle (PATCH) d'un athlète. Seuls les champs non nuls sont appliqués ;
 * les contraintes ne s'appliquent qu'aux champs présents.
 */
@Schema(description = "Requête de modification partielle d'un athlète (champs facultatifs)")
public record AthletePatchRequest(

        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
        String lastName,

        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères.")
        String firstName,

        Gender gender,

        @Past(message = "La date de naissance doit être dans le passé.")
        LocalDate birthDate,

        @Positive(message = "L'identifiant de la nationalité doit être positif.")
        Long countryId,

        @Positive(message = "L'identifiant de la discipline doit être positif.")
        Long disciplineId,

        @Min(value = 100, message = "La taille doit être comprise entre 100 et 260 cm.")
        @Max(value = 260, message = "La taille doit être comprise entre 100 et 260 cm.")
        Integer heightCm,

        @Min(value = 30, message = "Le poids doit être compris entre 30 et 250 kg.")
        @Max(value = 250, message = "Le poids doit être compris entre 30 et 250 kg.")
        Integer weightKg
) {
}

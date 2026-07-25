package com.jodak.dtos.country;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Données de création d'une nation.
 */
@Schema(description = "Requête de création d'une nation")
public record CountryRequest(

        @Schema(description = "Code ISO 3166-1 alpha-3 (3 lettres)", example = "FRA")
        @NotBlank(message = "Le code du pays est obligatoire.")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Le code du pays doit comporter exactement 3 lettres.")
        String code,

        @Schema(description = "Nom de la nation", example = "France")
        @NotBlank(message = "Le nom du pays est obligatoire.")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
        String name
) {
}

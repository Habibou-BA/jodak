package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Confirmation forte d'une réinitialisation : mot de passe + phrase de confirmation.
 */
@Schema(description = "Confirmation de réinitialisation")
public record ResetRequest(

        @NotBlank(message = "Le mot de passe est obligatoire.")
        String password,

        @Schema(description = "Phrase de confirmation exacte")
        @NotBlank(message = "La phrase de confirmation est obligatoire.")
        String confirmationPhrase
) {
}

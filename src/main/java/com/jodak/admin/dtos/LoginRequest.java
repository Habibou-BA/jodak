package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Identifiants de connexion administrateur")
public record LoginRequest(

        @Schema(example = "admin@jodak.sn")
        @NotBlank(message = "L'email est obligatoire.")
        @Email(message = "L'email doit être valide.")
        String email,

        @Schema(example = "••••••••")
        @NotBlank(message = "Le mot de passe est obligatoire.")
        String password
) {
}

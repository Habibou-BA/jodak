package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Demande de rafraîchissement du jeton d'accès")
public record RefreshRequest(

        @NotBlank(message = "Le refresh token est obligatoire.")
        String refreshToken
) {
}

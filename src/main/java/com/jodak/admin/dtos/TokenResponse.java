package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Jetons émis à la connexion / au rafraîchissement.
 */
@Schema(description = "Jetons d'authentification")
public record TokenResponse(
        @Schema(example = "Bearer") String tokenType,
        String accessToken,
        String refreshToken,
        @Schema(description = "Durée de validité de l'access token (secondes)") long expiresIn,
        @Schema(description = "L'administrateur doit-il changer son mot de passe ?") boolean mustChangePassword
) {
}

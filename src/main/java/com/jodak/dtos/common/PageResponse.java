package com.jodak.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Enveloppe de pagination homogène pour toutes les listes de l'API.
 *
 * @param <T> type des éléments de la page
 */
@Schema(description = "Réponse paginée avec métadonnées")
public record PageResponse<T>(
        List<T> content,
        @Schema(description = "Index de la page courante (0-based)") int page,
        @Schema(description = "Taille de la page") int size,
        @Schema(description = "Nombre total d'éléments") long totalElements,
        @Schema(description = "Nombre total de pages") int totalPages,
        @Schema(description = "Première page ?") boolean first,
        @Schema(description = "Dernière page ?") boolean last
) {
}

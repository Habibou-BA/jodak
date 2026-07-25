package com.jodak.dtos.resultat;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Podium d'une épreuve : les médaillés (rangs 1 à 3) ordonnés par rang croissant.
 */
@Schema(description = "Podium d'une épreuve")
public record PodiumResponse(
        Long epreuveId,
        String epreuveLabel,
        List<PodiumPosition> positions
) {
}

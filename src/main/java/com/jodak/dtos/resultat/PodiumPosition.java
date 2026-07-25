package com.jodak.dtos.resultat;

import com.jodak.dtos.athlete.AthleteSummary;
import com.jodak.enums.Medal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Une position du podium : rang, médaille et athlète.
 */
@Schema(description = "Position du podium")
public record PodiumPosition(
        Integer rank,
        Medal medal,
        AthleteSummary athlete
) {
}

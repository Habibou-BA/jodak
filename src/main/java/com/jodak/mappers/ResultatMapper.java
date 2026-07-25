package com.jodak.mappers;

import com.jodak.dtos.resultat.PodiumPosition;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Conversion manuelle entre l'entité {@link Resultat} et ses DTO. Les références (épreuve, athlète)
 * sont résolues par le service ; la médaille est calculée par l'entité (RM-12), jamais mappée
 * depuis la requête.
 */
@Component
@RequiredArgsConstructor
public class ResultatMapper {

    private final EpreuveMapper epreuveMapper;
    private final AthleteMapper athleteMapper;

    public Resultat toEntity(ResultatRequest request, Epreuve epreuve, Athlete athlete) {
        return Resultat.builder()
                .epreuve(epreuve)
                .athlete(athlete)
                .rankPosition(request.rank())
                .build();
    }

    public void updateEntity(Resultat entity, ResultatRequest request, Epreuve epreuve, Athlete athlete) {
        entity.setEpreuve(epreuve);
        entity.setAthlete(athlete);
        entity.setRankPosition(request.rank());
    }

    public ResultatResponse toResponse(Resultat entity) {
        return new ResultatResponse(
                entity.getId(),
                epreuveMapper.toSummary(entity.getEpreuve()),
                athleteMapper.toSummary(entity.getAthlete()),
                entity.getRankPosition(),
                entity.getMedal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public PodiumPosition toPodiumPosition(Resultat entity) {
        return new PodiumPosition(
                entity.getRankPosition(),
                entity.getMedal(),
                athleteMapper.toSummary(entity.getAthlete()));
    }
}

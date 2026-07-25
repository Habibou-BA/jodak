package com.jodak.mappers;

import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Conversion manuelle entre l'entité {@link Epreuve} et ses DTO. La discipline est résolue par le
 * service et fournie déjà chargée.
 */
@Component
@RequiredArgsConstructor
public class EpreuveMapper {

    private final DisciplineMapper disciplineMapper;

    public Epreuve toEntity(EpreuveRequest request, Discipline discipline) {
        return Epreuve.builder()
                .label(request.label().trim())
                .discipline(discipline)
                .eventDate(request.eventDate())
                .build();
    }

    public void updateEntity(Epreuve entity, EpreuveRequest request, Discipline discipline) {
        entity.setLabel(request.label().trim());
        entity.setDiscipline(discipline);
        entity.setEventDate(request.eventDate());
    }

    public EpreuveResponse toResponse(Epreuve entity) {
        return new EpreuveResponse(
                entity.getId(),
                entity.getLabel(),
                disciplineMapper.toSummary(entity.getDiscipline()),
                entity.getEventDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

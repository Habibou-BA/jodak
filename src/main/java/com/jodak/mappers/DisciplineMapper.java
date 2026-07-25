package com.jodak.mappers;

import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.dtos.discipline.DisciplineSummary;
import com.jodak.entities.Discipline;
import org.springframework.stereotype.Component;

/**
 * Conversion manuelle entre l'entité {@link Discipline} et ses DTO.
 */
@Component
public class DisciplineMapper {

    public Discipline toEntity(DisciplineRequest request) {
        Discipline discipline = new Discipline();
        discipline.setName(normalize(request.name()));
        return discipline;
    }

    public void updateEntity(Discipline entity, DisciplineRequest request) {
        entity.setName(normalize(request.name()));
    }

    public DisciplineResponse toResponse(Discipline entity) {
        return new DisciplineResponse(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public DisciplineSummary toSummary(Discipline entity) {
        return new DisciplineSummary(entity.getId(), entity.getName());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}

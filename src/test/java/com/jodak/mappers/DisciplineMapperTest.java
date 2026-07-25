package com.jodak.mappers;

import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.entities.Discipline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisciplineMapperTest {

    private final DisciplineMapper mapper = new DisciplineMapper();

    @Test
    @DisplayName("toEntity nettoie les espaces superflus du nom")
    void toEntityTrimsName() {
        Discipline entity = mapper.toEntity(new DisciplineRequest("  Athlétisme  "));

        assertThat(entity.getName()).isEqualTo("Athlétisme");
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("updateEntity remplace le nom de l'entité existante")
    void updateEntityReplacesName() {
        Discipline entity = Discipline.builder().id(1L).name("Ancien").build();

        mapper.updateEntity(entity, new DisciplineRequest(" Natation "));

        assertThat(entity.getName()).isEqualTo("Natation");
        assertThat(entity.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toResponse projette tous les champs")
    void toResponseMapsAllFields() {
        Discipline entity = Discipline.builder().id(7L).name("Judo").build();

        DisciplineResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("Judo");
    }
}

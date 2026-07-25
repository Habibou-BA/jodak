package com.jodak.mappers;

import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EpreuveMapperTest {

    private final EpreuveMapper mapper = new EpreuveMapper(new DisciplineMapper());

    @Test
    @DisplayName("toEntity nettoie le libellé et affecte la discipline résolue")
    void toEntityMapsFields() {
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        EpreuveRequest request = new EpreuveRequest("  100 m  ", 1L, LocalDate.of(2024, 8, 4));

        Epreuve entity = mapper.toEntity(request, discipline);

        assertThat(entity.getLabel()).isEqualTo("100 m");
        assertThat(entity.getDiscipline()).isSameAs(discipline);
        assertThat(entity.getEventDate()).isEqualTo(LocalDate.of(2024, 8, 4));
    }

    @Test
    @DisplayName("toResponse imbrique le résumé de la discipline")
    void toResponseNestsDisciplineSummary() {
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        Epreuve entity = Epreuve.builder()
                .id(9L).label("100 m").discipline(discipline).eventDate(LocalDate.of(2024, 8, 4))
                .build();

        EpreuveResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.label()).isEqualTo("100 m");
        assertThat(response.discipline().name()).isEqualTo("Athlétisme");
    }
}

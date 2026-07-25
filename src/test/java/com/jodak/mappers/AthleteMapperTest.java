package com.jodak.mappers;

import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteMapperTest {

    private final AthleteMapper mapper = new AthleteMapper(new CountryMapper(), new DisciplineMapper());

    @Test
    @DisplayName("toEntity affecte les champs et les références résolues")
    void toEntityMapsFields() {
        Country country = Country.builder().id(3L).code("FRA").name("France").build();
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        AthleteRequest request = new AthleteRequest(
                " Bolt ", " Usain ", Gender.MALE, LocalDate.of(1986, 8, 21), 3L, 1L, 195, 94);

        Athlete entity = mapper.toEntity(request, country, discipline);

        assertThat(entity.getLastName()).isEqualTo("Bolt");
        assertThat(entity.getFirstName()).isEqualTo("Usain");
        assertThat(entity.getCountry()).isSameAs(country);
        assertThat(entity.getDiscipline()).isSameAs(discipline);
        assertThat(entity.getHeightCm()).isEqualTo(195);
    }

    @Test
    @DisplayName("toResponse imbrique les résumés et calcule l'âge")
    void toResponseNestsSummariesAndAge() {
        Country country = Country.builder().id(3L).code("FRA").name("France").build();
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        Athlete entity = Athlete.builder()
                .id(10L).lastName("Bolt").firstName("Usain").gender(Gender.MALE)
                .birthDate(LocalDate.now().minusYears(20))
                .country(country).discipline(discipline).heightCm(195).weightKg(94)
                .build();

        AthleteResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.country().code()).isEqualTo("FRA");
        assertThat(response.discipline().name()).isEqualTo("Athlétisme");
        assertThat(response.age()).isEqualTo(20);
    }
}

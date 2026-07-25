package com.jodak.mappers;

import com.jodak.dtos.resultat.PodiumPosition;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import com.jodak.enums.Medal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultatMapperTest {

    private final ResultatMapper mapper = new ResultatMapper(
            new EpreuveMapper(new DisciplineMapper()),
            new AthleteMapper(new CountryMapper(), new DisciplineMapper()));

    private Resultat sample() {
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        Epreuve epreuve = Epreuve.builder().id(2L).label("100 m").discipline(discipline).build();
        Athlete athlete = Athlete.builder().id(3L).lastName("Bolt").firstName("Usain").build();
        return Resultat.builder()
                .id(5L).epreuve(epreuve).athlete(athlete).rankPosition(1).medal(Medal.OR)
                .build();
    }

    @Test
    @DisplayName("toResponse imbrique les résumés et expose rang + médaille")
    void toResponseMapsFields() {
        ResultatResponse response = mapper.toResponse(sample());

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.epreuve().label()).isEqualTo("100 m");
        assertThat(response.athlete().lastName()).isEqualTo("Bolt");
        assertThat(response.rank()).isEqualTo(1);
        assertThat(response.medal()).isEqualTo(Medal.OR);
    }

    @Test
    @DisplayName("toPodiumPosition expose rang, médaille et athlète")
    void toPodiumPositionMapsFields() {
        PodiumPosition position = mapper.toPodiumPosition(sample());

        assertThat(position.rank()).isEqualTo(1);
        assertThat(position.medal()).isEqualTo(Medal.OR);
        assertThat(position.athlete().firstName()).isEqualTo("Usain");
    }
}

package com.jodak.soap;

import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.discipline.DisciplineSummary;
import com.jodak.enums.Gender;
import com.jodak.services.interfaces.AthleteService;
import com.jodak.soap.endpoints.AthleteSoapEndpoint;
import com.jodak.soap.generated.GetAthleteRequest;
import com.jodak.soap.generated.GetAthleteResponse;
import com.jodak.soap.mappers.SoapMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AthleteSoapEndpointTest {

    private final AthleteService athleteService = mock(AthleteService.class);
    private final AthleteSoapEndpoint endpoint = new AthleteSoapEndpoint(athleteService, new SoapMapper());

    @Test
    @DisplayName("getAthlete convertit le DTO en objet XML (JAXB)")
    void getAthleteMapsToSoap() {
        when(athleteService.getById(1L)).thenReturn(new AthleteResponse(
                1L, "Bolt", "Usain", Gender.MALE, LocalDate.of(1986, 8, 21), 39,
                new CountrySummary(3L, "FRA", "France"),
                new DisciplineSummary(1L, "Athlétisme"), 195, 94, null, null));

        GetAthleteRequest request = new GetAthleteRequest();
        request.setId(1L);

        GetAthleteResponse response = endpoint.getAthlete(request);

        assertThat(response.getAthlete().getLastName()).isEqualTo("Bolt");
        assertThat(response.getAthlete().getGender()).isEqualTo("MALE");
        assertThat(response.getAthlete().getNationalityCode()).isEqualTo("FRA");
        assertThat(response.getAthlete().getDiscipline()).isEqualTo("Athlétisme");
        assertThat(response.getAthlete().getHeightCm()).isEqualTo(195);
    }
}

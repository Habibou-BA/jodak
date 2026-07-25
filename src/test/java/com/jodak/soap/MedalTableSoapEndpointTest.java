package com.jodak.soap;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.services.interfaces.MedalTableService;
import com.jodak.soap.endpoints.MedalTableSoapEndpoint;
import com.jodak.soap.generated.GetMedalTableRequest;
import com.jodak.soap.generated.GetMedalTableResponse;
import com.jodak.soap.mappers.SoapMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedalTableSoapEndpointTest {

    private final MedalTableService medalTableService = mock(MedalTableService.class);
    private final MedalTableSoapEndpoint endpoint = new MedalTableSoapEndpoint(medalTableService, new SoapMapper());

    @Test
    @DisplayName("getMedalTable convertit chaque ligne en objet XML (JAXB)")
    void getMedalTableMapsToSoap() {
        when(medalTableService.getMedalTable()).thenReturn(List.of(
                new MedalTableRowResponse(1, new CountrySummary(1L, "USA", "États-Unis"), 12, 8, 5, 25)));

        GetMedalTableResponse response = endpoint.getMedalTable(new GetMedalTableRequest());

        assertThat(response.getRow()).hasSize(1);
        assertThat(response.getRow().get(0).getCountryCode()).isEqualTo("USA");
        assertThat(response.getRow().get(0).getGold()).isEqualTo(12);
        assertThat(response.getRow().get(0).getTotal()).isEqualTo(25);
    }
}

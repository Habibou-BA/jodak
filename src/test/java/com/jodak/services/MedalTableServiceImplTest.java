package com.jodak.services;

import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.repositories.ResultatRepository;
import com.jodak.repositories.projections.CountryMedalCount;
import com.jodak.services.implementations.MedalTableServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedalTableServiceImplTest {

    @Mock
    private ResultatRepository resultatRepository;

    @InjectMocks
    private MedalTableServiceImpl service;

    private static CountryMedalCount agg(Long id, String code, String name, long g, long s, long b) {
        return new CountryMedalCount() {
            public Long getCountryId() {
                return id;
            }

            public String getCountryCode() {
                return code;
            }

            public String getCountryName() {
                return name;
            }

            public long getGold() {
                return g;
            }

            public long getSilver() {
                return s;
            }

            public long getBronze() {
                return b;
            }
        };
    }

    @Test
    @DisplayName("Classe par or, puis argent, puis bronze et calcule le total et le rang")
    void ranksByOfficialRules() {
        when(resultatRepository.aggregateMedalsByCountry()).thenReturn(new ArrayList<>(List.of(
                agg(1L, "AAA", "Alpha", 1, 0, 0),
                agg(2L, "BBB", "Beta", 1, 1, 0))));

        List<MedalTableRowResponse> table = service.getMedalTable();

        assertThat(table).hasSize(2);
        assertThat(table.get(0).rank()).isEqualTo(1);
        assertThat(table.get(0).country().code()).isEqualTo("BBB");
        assertThat(table.get(0).total()).isEqualTo(2);
        assertThat(table.get(1).rank()).isEqualTo(2);
        assertThat(table.get(1).country().code()).isEqualTo("AAA");
        assertThat(table.get(1).total()).isEqualTo(1);
    }

    @Test
    @DisplayName("Départage les égalités complètes par ordre alphabétique de nation (D7)")
    void breaksTiesAlphabetically() {
        when(resultatRepository.aggregateMedalsByCountry()).thenReturn(new ArrayList<>(List.of(
                agg(1L, "BBB", "Beta", 2, 1, 1),
                agg(2L, "AAA", "Alpha", 2, 1, 1))));

        List<MedalTableRowResponse> table = service.getMedalTable();

        assertThat(table.get(0).country().name()).isEqualTo("Alpha");
        assertThat(table.get(1).country().name()).isEqualTo("Beta");
    }
}

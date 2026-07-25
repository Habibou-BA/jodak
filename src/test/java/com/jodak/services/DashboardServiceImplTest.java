package com.jodak.services;

import com.jodak.dtos.dashboard.DashboardResponse;
import com.jodak.enums.Medal;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import com.jodak.repositories.projections.CountryMedalCount;
import com.jodak.services.implementations.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private AthleteRepository athleteRepository;
    @Mock
    private DisciplineRepository disciplineRepository;
    @Mock
    private EpreuveRepository epreuveRepository;
    @Mock
    private ResultatRepository resultatRepository;

    @InjectMocks
    private DashboardServiceImpl service;

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
    @DisplayName("getDashboard agrège les compteurs et classe les nations par points")
    void computesDashboard() {
        when(athleteRepository.count()).thenReturn(3L);
        when(athleteRepository.countParticipatingCountries()).thenReturn(2L);
        when(disciplineRepository.count()).thenReturn(1L);
        when(epreuveRepository.count()).thenReturn(2L);
        when(resultatRepository.countByMedal(Medal.OR)).thenReturn(2L);
        when(resultatRepository.countByMedal(Medal.ARGENT)).thenReturn(1L);
        when(resultatRepository.countByMedal(Medal.BRONZE)).thenReturn(0L);
        when(resultatRepository.aggregateMedalsByCountry()).thenReturn(List.of(
                agg(1L, "AAA", "Alpha", 1, 0, 0),
                agg(2L, "BBB", "Beta", 1, 1, 0)));

        DashboardResponse dashboard = service.getDashboard();

        assertThat(dashboard.totalAthletes()).isEqualTo(3);
        assertThat(dashboard.totalCountries()).isEqualTo(2);
        assertThat(dashboard.totalDisciplines()).isEqualTo(1);
        assertThat(dashboard.totalEpreuves()).isEqualTo(2);
        assertThat(dashboard.totalGold()).isEqualTo(2);
        assertThat(dashboard.totalMedals()).isEqualTo(3);

        assertThat(dashboard.pointsRanking()).hasSize(2);
        assertThat(dashboard.pointsRanking().get(0).country().code()).isEqualTo("BBB");
        assertThat(dashboard.pointsRanking().get(0).points()).isEqualTo(11); // 7 + 4
        assertThat(dashboard.pointsRanking().get(1).country().code()).isEqualTo("AAA");
        assertThat(dashboard.pointsRanking().get(1).points()).isEqualTo(7);
    }
}

package com.jodak.services.implementations;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.dashboard.DashboardResponse;
import com.jodak.dtos.dashboard.PointsRankingRow;
import com.jodak.enums.Medal;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import com.jodak.repositories.projections.CountryMedalCount;
import com.jodak.services.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calcule les statistiques du tableau de bord. Réutilise l'agrégation des médailles par nation
 * pour le classement par points (barème porté par {@link Medal}).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AthleteRepository athleteRepository;
    private final DisciplineRepository disciplineRepository;
    private final EpreuveRepository epreuveRepository;
    private final ResultatRepository resultatRepository;

    @Override
    public DashboardResponse getDashboard() {
        long gold = resultatRepository.countByMedal(Medal.OR);
        long silver = resultatRepository.countByMedal(Medal.ARGENT);
        long bronze = resultatRepository.countByMedal(Medal.BRONZE);

        return new DashboardResponse(
                athleteRepository.count(),
                athleteRepository.countParticipatingCountries(),
                disciplineRepository.count(),
                epreuveRepository.count(),
                gold, silver, bronze, gold + silver + bronze,
                getPointsRanking());
    }

    @Override
    public List<PointsRankingRow> getPointsRanking() {
        record Scored(CountryMedalCount country, long points) {
        }

        Comparator<Scored> byPoints = Comparator.comparingLong(Scored::points).reversed()
                .thenComparing(Comparator.comparingLong((Scored s) -> s.country().getGold()).reversed())
                .thenComparing(s -> s.country().getCountryName());

        List<Scored> scored = resultatRepository.aggregateMedalsByCountry().stream()
                .map(c -> new Scored(c, points(c)))
                .sorted(byPoints)
                .toList();

        List<PointsRankingRow> ranking = new ArrayList<>(scored.size());
        int rank = 1;
        for (Scored s : scored) {
            CountryMedalCount c = s.country();
            long total = c.getGold() + c.getSilver() + c.getBronze();
            ranking.add(new PointsRankingRow(
                    rank++,
                    new CountrySummary(c.getCountryId(), c.getCountryCode(), c.getCountryName()),
                    s.points(), c.getGold(), c.getSilver(), c.getBronze(), total));
        }
        return ranking;
    }

    private long points(CountryMedalCount c) {
        return c.getGold() * Medal.OR.getPoints()
                + c.getSilver() * Medal.ARGENT.getPoints()
                + c.getBronze() * Medal.BRONZE.getPoints();
    }
}

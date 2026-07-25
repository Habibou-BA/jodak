package com.jodak.services.implementations;

import com.jodak.dtos.country.CountrySummary;
import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.repositories.ResultatRepository;
import com.jodak.repositories.projections.CountryMedalCount;
import com.jodak.services.interfaces.MedalTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcule le tableau officiel des médailles : agrégation par nation (SQL) puis classement selon
 * les règles officielles (or, argent, bronze) avec départage alphabétique déterministe (RM-15 / D7).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedalTableServiceImpl implements MedalTableService {

    private final ResultatRepository resultatRepository;

    /** Classement officiel : or décroissant, puis argent, puis bronze, puis nation A→Z (D7). */
    private static final Comparator<CountryMedalCount> OFFICIAL_RANKING =
            Comparator.comparingLong(CountryMedalCount::getGold).reversed()
                    .thenComparing(Comparator.comparingLong(CountryMedalCount::getSilver).reversed())
                    .thenComparing(Comparator.comparingLong(CountryMedalCount::getBronze).reversed())
                    .thenComparing(CountryMedalCount::getCountryName);

    @Override
    public List<MedalTableRowResponse> getMedalTable() {
        List<CountryMedalCount> aggregates = resultatRepository.aggregateMedalsByCountry();
        aggregates.sort(OFFICIAL_RANKING);

        List<MedalTableRowResponse> table = new ArrayList<>(aggregates.size());
        int rank = 1;
        for (CountryMedalCount row : aggregates) {
            long total = row.getGold() + row.getSilver() + row.getBronze();
            table.add(new MedalTableRowResponse(
                    rank++,
                    new CountrySummary(row.getCountryId(), row.getCountryCode(), row.getCountryName()),
                    row.getGold(), row.getSilver(), row.getBronze(), total));
        }
        log.debug("Tableau des médailles calculé : {} nation(s) médaillée(s)", table.size());
        return table;
    }
}

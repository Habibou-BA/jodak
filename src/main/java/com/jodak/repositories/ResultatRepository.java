package com.jodak.repositories;

import com.jodak.entities.Resultat;
import com.jodak.enums.Medal;
import com.jodak.repositories.projections.CountryMedalCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Accès aux données des résultats.
 */
public interface ResultatRepository
        extends JpaRepository<Resultat, Long>, JpaSpecificationExecutor<Resultat> {

    boolean existsByEpreuve_IdAndAthlete_Id(Long epreuveId, Long athleteId);

    boolean existsByEpreuve_IdAndAthlete_IdAndIdNot(Long epreuveId, Long athleteId, Long id);

    boolean existsByEpreuve_IdAndRankPosition(Long epreuveId, Integer rankPosition);

    boolean existsByEpreuve_IdAndRankPositionAndIdNot(Long epreuveId, Integer rankPosition, Long id);

    /** Podium : les médaillés d'une épreuve, ordonnés par rang croissant. */
    List<Resultat> findByEpreuve_IdAndMedalNotNullOrderByRankPositionAsc(Long epreuveId);

    /**
     * Agrégation du nombre de médailles par nation (RM-14). Le tri et le classement sont appliqués
     * dans le service afin de maîtriser les règles de départage (RM-15 / D7).
     */
    @Query("""
            select c.id as countryId, c.code as countryCode, c.name as countryName,
                   sum(case when r.medal = com.jodak.enums.Medal.OR     then 1 else 0 end) as gold,
                   sum(case when r.medal = com.jodak.enums.Medal.ARGENT then 1 else 0 end) as silver,
                   sum(case when r.medal = com.jodak.enums.Medal.BRONZE then 1 else 0 end) as bronze
            from Resultat r
              join r.athlete a
              join a.country c
            where r.medal is not null
            group by c.id, c.code, c.name
            """)
    List<CountryMedalCount> aggregateMedalsByCountry();

    /** Nombre total de médailles d'un type donné (or, argent ou bronze). */
    long countByMedal(Medal medal);
}

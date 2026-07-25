package com.jodak.repositories;

import com.jodak.entities.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}

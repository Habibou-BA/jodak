package com.jodak.specifications;

import com.jodak.dtos.resultat.ResultatSearchCriteria;
import com.jodak.entities.Resultat;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construction des {@link Specification} de recherche des résultats (Criteria API).
 */
public final class ResultatSpecifications {

    private ResultatSpecifications() {
    }

    public static Specification<Resultat> build(ResultatSearchCriteria criteria) {
        List<Specification<Resultat>> specs = new ArrayList<>();
        if (criteria.epreuveId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("epreuve").get("id"), criteria.epreuveId()));
        }
        if (criteria.athleteId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("athlete").get("id"), criteria.athleteId()));
        }
        if (criteria.medal() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("medal"), criteria.medal()));
        }
        return specs.stream().reduce(Specification.where(null), Specification::and);
    }
}

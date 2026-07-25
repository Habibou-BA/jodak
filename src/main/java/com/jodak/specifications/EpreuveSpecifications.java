package com.jodak.specifications;

import com.jodak.dtos.epreuve.EpreuveSearchCriteria;
import com.jodak.entities.Epreuve;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construction des {@link Specification} de recherche multicritère des épreuves (Criteria API),
 * couvrant notamment la recherche par discipline et par date.
 */
public final class EpreuveSpecifications {

    private EpreuveSpecifications() {
    }

    public static Specification<Epreuve> build(EpreuveSearchCriteria criteria) {
        List<Specification<Epreuve>> specs = new ArrayList<>();
        if (hasText(criteria.label())) {
            String pattern = "%" + criteria.label().trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("label")), pattern));
        }
        if (criteria.disciplineId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("discipline").get("id"), criteria.disciplineId()));
        }
        if (criteria.eventDate() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("eventDate"), criteria.eventDate()));
        }
        if (criteria.eventDateFrom() != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), criteria.eventDateFrom()));
        }
        if (criteria.eventDateTo() != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), criteria.eventDateTo()));
        }
        return specs.stream().reduce(Specification.where(null), Specification::and);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

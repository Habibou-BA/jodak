package com.jodak.specifications;

import com.jodak.dtos.athlete.AthleteSearchCriteria;
import com.jodak.entities.Athlete;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construction des {@link Specification} de recherche multicritère des athlètes (Criteria API).
 */
public final class AthleteSpecifications {

    private AthleteSpecifications() {
    }

    public static Specification<Athlete> build(AthleteSearchCriteria criteria) {
        List<Specification<Athlete>> specs = new ArrayList<>();
        if (hasText(criteria.lastName())) {
            specs.add(lastNameContains(criteria.lastName()));
        }
        if (hasText(criteria.firstName())) {
            specs.add(firstNameContains(criteria.firstName()));
        }
        if (criteria.gender() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("gender"), criteria.gender()));
        }
        if (criteria.countryId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("country").get("id"), criteria.countryId()));
        }
        if (criteria.disciplineId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("discipline").get("id"), criteria.disciplineId()));
        }
        if (criteria.birthDateFrom() != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("birthDate"), criteria.birthDateFrom()));
        }
        if (criteria.birthDateTo() != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("birthDate"), criteria.birthDateTo()));
        }
        return specs.stream().reduce(Specification.where(null), Specification::and);
    }

    private static Specification<Athlete> lastNameContains(String value) {
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("lastName")), pattern);
    }

    private static Specification<Athlete> firstNameContains(String value) {
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("firstName")), pattern);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

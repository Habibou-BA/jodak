package com.jodak.repositories;

import com.jodak.entities.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Accès aux données des athlètes. Le {@link JpaSpecificationExecutor} permet la recherche
 * multicritère par {@code Specification} (aucune prolifération de méthodes).
 */
public interface AthleteRepository
        extends JpaRepository<Athlete, Long>, JpaSpecificationExecutor<Athlete> {
}

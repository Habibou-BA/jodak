package com.jodak.repositories;

import com.jodak.entities.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Accès aux données des athlètes. Le {@link JpaSpecificationExecutor} permet la recherche
 * multicritère par {@code Specification} (aucune prolifération de méthodes).
 */
public interface AthleteRepository
        extends JpaRepository<Athlete, Long>, JpaSpecificationExecutor<Athlete> {

    /** Nombre de nations distinctes ayant au moins un athlète (pays participants). */
    @Query("select count(distinct a.country.id) from Athlete a")
    long countParticipatingCountries();

    boolean existsByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndBirthDate(
            String lastName, String firstName, java.time.LocalDate birthDate);

    java.util.Optional<Athlete> findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndBirthDate(
            String lastName, String firstName, java.time.LocalDate birthDate);
}

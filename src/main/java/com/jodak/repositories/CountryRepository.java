package com.jodak.repositories;

import com.jodak.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Accès aux données du référentiel des nations.
 */
public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    Optional<Country> findByCodeIgnoreCase(String code);
}

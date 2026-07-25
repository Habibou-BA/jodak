package com.jodak.repositories;

import com.jodak.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données du référentiel des nations.
 */
public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);
}

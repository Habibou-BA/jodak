package com.jodak.repositories;

import com.jodak.entities.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des disciplines.
 */
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

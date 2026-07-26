package com.jodak.repositories;

import com.jodak.entities.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Accès aux données des disciplines.
 */
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Discipline> findByNameIgnoreCase(String name);
}

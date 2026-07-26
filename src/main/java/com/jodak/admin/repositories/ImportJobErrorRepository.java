package com.jodak.admin.repositories;

import com.jodak.admin.entities.ImportJobError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux erreurs de lignes d'un import.
 */
public interface ImportJobErrorRepository extends JpaRepository<ImportJobError, Long> {

    Page<ImportJobError> findByImportJobIdOrderByRowNumberAsc(Long importJobId, Pageable pageable);
}

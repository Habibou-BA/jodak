package com.jodak.admin.repositories;

import com.jodak.admin.entities.ImportJobRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Accès aux enregistrements de compensation d'un import.
 */
public interface ImportJobRecordRepository extends JpaRepository<ImportJobRecord, Long> {

    List<ImportJobRecord> findByImportJobIdOrderByIdDesc(Long importJobId);
}

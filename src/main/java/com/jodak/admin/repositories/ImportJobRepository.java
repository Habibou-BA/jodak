package com.jodak.admin.repositories;

import com.jodak.admin.entities.ImportJob;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;

/**
 * Accès aux jobs d'import. Les mises à jour de progression/statut sont des requêtes ciblées
 * (elles ne touchent jamais {@code cancel_requested}) afin d'éviter tout conflit avec une
 * demande d'annulation concurrente.
 */
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    Page<ImportJob> findByStatus(ImportStatus status, Pageable pageable);

    long countByStatus(ImportStatus status);

    boolean existsByContentHashAndJobTypeAndStatusIn(
            String contentHash, ImportJobType jobType, Collection<ImportStatus> statuses);

    @Query("select j.cancelRequested from ImportJob j where j.id = :id")
    Boolean isCancelRequested(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("update ImportJob j set j.cancelRequested = true where j.id = :id")
    void requestCancel(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("""
            update ImportJob j
               set j.status = com.jodak.admin.enums.ImportStatus.RUNNING,
                   j.totalRows = :total, j.startedAt = :startedAt
             where j.id = :id""")
    void markRunning(@Param("id") Long id, @Param("total") long total,
                     @Param("startedAt") OffsetDateTime startedAt);

    @Transactional
    @Modifying
    @Query("""
            update ImportJob j
               set j.processedRows = :processed, j.importedRows = :imported, j.updatedRows = :updated,
                   j.skippedRows = :skipped, j.failedRows = :failed, j.progressPercent = :percent
             where j.id = :id""")
    void saveProgress(@Param("id") Long id, @Param("processed") long processed,
                      @Param("imported") long imported, @Param("updated") long updated,
                      @Param("skipped") long skipped, @Param("failed") long failed,
                      @Param("percent") int percent);

    @Transactional
    @Modifying
    @Query("""
            update ImportJob j
               set j.status = :status, j.processedRows = :processed, j.importedRows = :imported,
                   j.updatedRows = :updated, j.skippedRows = :skipped, j.failedRows = :failed,
                   j.progressPercent = :percent, j.finishedAt = :finishedAt, j.durationMs = :durationMs
             where j.id = :id""")
    void finish(@Param("id") Long id, @Param("status") ImportStatus status,
                @Param("processed") long processed, @Param("imported") long imported,
                @Param("updated") long updated, @Param("skipped") long skipped,
                @Param("failed") long failed, @Param("percent") int percent,
                @Param("finishedAt") OffsetDateTime finishedAt, @Param("durationMs") long durationMs);

    @Transactional
    @Modifying
    @Query("""
            update ImportJob j
               set j.status = com.jodak.admin.enums.ImportStatus.FAILED,
                   j.errorMessage = :message, j.finishedAt = :finishedAt, j.durationMs = :durationMs
             where j.id = :id""")
    void markFailed(@Param("id") Long id, @Param("message") String message,
                    @Param("finishedAt") OffsetDateTime finishedAt, @Param("durationMs") long durationMs);
}

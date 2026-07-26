package com.jodak.admin.dataimport;

import com.jodak.admin.entities.ImportJob;
import com.jodak.admin.entities.ImportJobError;
import com.jodak.admin.entities.ImportJobRecord;
import com.jodak.admin.enums.ImportStatus;
import com.jodak.admin.repositories.ImportJobErrorRepository;
import com.jodak.admin.repositories.ImportJobRecordRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Exécute un job d'import en arrière-plan : validation des colonnes, comptage, traitement par lots
 * avec suivi de progression, annulation coopérative, compensation et rapport d'erreurs.
 *
 * <p>Non {@code @Transactional} : chaque écriture (entité importée, progression, erreur) est
 * committée immédiatement — ce qui donne la sémantique « une ligne fautive est ignorée, les bonnes
 * sont importées » et rend la progression visible en quasi temps réel.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportExecutor {

    private final ImportJobRepository importJobRepository;
    private final ImportJobErrorRepository importJobErrorRepository;
    private final ImportJobRecordRepository importJobRecordRepository;
    private final RowReaderFactory readerFactory;
    private final RowImporterRegistry registry;
    private final ImportProperties properties;

    @Async(AsyncImportConfig.IMPORT_EXECUTOR)
    public void run(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        MDC.put("jobId", String.valueOf(jobId));
        MDC.put("correlationId", job.getCorrelationId());
        long start = System.currentTimeMillis();
        try {
            RowImporter importer = registry.forType(job.getJobType());
            Path path = Path.of(job.getStoredFilePath());

            long total;
            try (RowReader reader = readerFactory.open(path, job.getFormat())) {
                List<String> missing = missingColumns(importer.requiredColumns(), reader.headers());
                if (!missing.isEmpty()) {
                    fail(jobId, "Colonnes manquantes : " + String.join(", ", missing), start);
                    return;
                }
                total = count(reader);
            }
            importJobRepository.markRunning(jobId, total, OffsetDateTime.now());

            Counters c = new Counters();
            int chunk = Math.max(1, properties.chunkSize());
            long sinceUpdate = 0;

            try (RowReader reader = readerFactory.open(path, job.getFormat())) {
                for (RowData row : reader) {
                    if (sinceUpdate == 0 && Boolean.TRUE.equals(importJobRepository.isCancelRequested(jobId))) {
                        importJobRepository.finish(jobId, ImportStatus.CANCELLED, c.processed, c.imported,
                                c.updated, c.skipped, c.failed, percent(c.processed, total),
                                OffsetDateTime.now(), System.currentTimeMillis() - start);
                        log.info("Import job {} annulé après {} lignes", jobId, c.processed);
                        return;
                    }
                    apply(jobId, importer, row, job, c);
                    c.processed++;
                    if (++sinceUpdate >= chunk) {
                        importJobRepository.saveProgress(jobId, c.processed, c.imported, c.updated,
                                c.skipped, c.failed, percent(c.processed, total));
                        sinceUpdate = 0;
                    }
                }
            }

            importJobRepository.finish(jobId, ImportStatus.COMPLETED, c.processed, c.imported, c.updated,
                    c.skipped, c.failed, 100, OffsetDateTime.now(), System.currentTimeMillis() - start);
            log.info("Import job {} terminé : {} importées, {} ignorées, {} en échec",
                    jobId, c.imported, c.skipped, c.failed);
        } catch (Exception ex) {
            log.error("Import job {} en échec", jobId, ex);
            fail(jobId, truncate(ex.getMessage()), start);
        } finally {
            MDC.clear();
        }
    }

    private void apply(Long jobId, RowImporter importer, RowData row, ImportJob job, Counters c) {
        ImportRowOutcome outcome;
        try {
            outcome = importer.importRow(row, job.getMode(), job.getDuplicateStrategy());
        } catch (Exception ex) {
            outcome = ImportRowOutcome.failed(null, "ERROR", truncate(ex.getMessage()));
        }
        switch (outcome.result()) {
            case IMPORTED -> {
                c.imported++;
                recordCompensation(jobId, outcome, "INSERT");
            }
            case UPDATED -> {
                c.updated++;
                recordCompensation(jobId, outcome, "UPDATE");
            }
            case SKIPPED -> c.skipped++;
            case FAILED -> {
                c.failed++;
                importJobErrorRepository.save(ImportJobError.builder()
                        .importJobId(jobId).rowNumber(row.number())
                        .columnName(outcome.column()).rawValue(rawValue(row, outcome.column()))
                        .errorCode(outcome.errorCode()).message(truncate(outcome.message()))
                        .build());
            }
        }
    }

    private void recordCompensation(Long jobId, ImportRowOutcome outcome, String operation) {
        if (outcome.entityId() != null) {
            importJobRecordRepository.save(ImportJobRecord.builder()
                    .importJobId(jobId).entityType(outcome.entityType())
                    .entityId(outcome.entityId()).operation(operation).build());
        }
    }

    private void fail(Long jobId, String message, long start) {
        importJobRepository.markFailed(jobId, message, OffsetDateTime.now(),
                System.currentTimeMillis() - start);
    }

    private long count(RowReader reader) {
        long n = 0;
        for (RowData ignored : reader) {
            n++;
        }
        return n;
    }

    private List<String> missingColumns(List<String> required, List<String> headers) {
        return required.stream()
                .filter(col -> headers.stream().noneMatch(h -> h.equalsIgnoreCase(col)))
                .toList();
    }

    private int percent(long processed, long total) {
        return total <= 0 ? 0 : (int) Math.min(100, (processed * 100) / total);
    }

    private String rawValue(RowData row, String column) {
        return column == null ? null : truncate(row.get(column));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    /** Compteurs mutables locaux au traitement. */
    private static final class Counters {
        long processed;
        long imported;
        long updated;
        long skipped;
        long failed;
    }
}

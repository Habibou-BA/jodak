package com.jodak.admin.dataimport;

import com.jodak.admin.entities.ImportJob;
import com.jodak.admin.entities.ImportJobError;
import com.jodak.admin.entities.ImportJobRecord;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportStatus;
import com.jodak.admin.repositories.ImportJobErrorRepository;
import com.jodak.admin.repositories.ImportJobRecordRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            if (job.getJobType() == ImportJobType.SYSTEME) {
                runSystem(job, start);
                return;
            }
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
                    apply(jobId, importer, row, job, c, null);
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

    /**
     * Applique un importeur à une ligne et met à jour les compteurs. {@code sheetLabel} (non nul en
     * import « système ») préfixe le message d'erreur pour identifier la feuille d'origine.
     */
    private void apply(Long jobId, RowImporter importer, RowData row, ImportJob job, Counters c,
                       String sheetLabel) {
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
                String message = sheetLabel == null ? outcome.message()
                        : "[" + sheetLabel + "] " + outcome.message();
                importJobErrorRepository.save(ImportJobError.builder()
                        .importJobId(jobId).rowNumber(row.number())
                        .columnName(outcome.column()).rawValue(rawValue(row, outcome.column()))
                        .errorCode(outcome.errorCode()).message(truncate(message))
                        .build());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Import « système » : un classeur XLSX multi-feuilles chargé en une passe.
    // ------------------------------------------------------------------------

    /** Feuilles reconnues, dans l'ordre des dépendances (nations → disciplines → épreuves → athlètes). */
    private static final List<SheetPlan> SYSTEM_PLAN = List.of(
            new SheetPlan("Nations", ImportJobType.COUNTRY),
            new SheetPlan("Disciplines", ImportJobType.DISCIPLINE),
            new SheetPlan("Epreuves", ImportJobType.EPREUVE),
            new SheetPlan("Athletes", ImportJobType.ATHLETE));

    private record SheetPlan(String sheet, ImportJobType type) {
    }

    private void runSystem(ImportJob job, long start) {
        Long jobId = job.getId();
        Path path = Path.of(job.getStoredFilePath());
        try {
            List<String> sheetNames;
            try {
                sheetNames = readerFactory.sheetNames(path);
            } catch (IOException ex) {
                fail(jobId, "Classeur illisible : " + truncate(ex.getMessage()), start);
                return;
            }

            // Ne conserve que les feuilles attendues réellement présentes (nom réel du classeur).
            List<SheetPlan> present = new ArrayList<>();
            for (SheetPlan p : SYSTEM_PLAN) {
                sheetNames.stream().filter(n -> n.equalsIgnoreCase(p.sheet())).findFirst()
                        .ifPresent(actual -> present.add(new SheetPlan(actual, p.type())));
            }
            if (present.isEmpty()) {
                fail(jobId, "Aucune feuille reconnue (attendu : "
                        + SYSTEM_PLAN.stream().map(SheetPlan::sheet).collect(Collectors.joining(", ")) + ").", start);
                return;
            }

            // Validation des colonnes + comptage total (une passe de lecture par feuille).
            long total = 0;
            for (SheetPlan p : present) {
                RowImporter importer = registry.forType(p.type());
                try (RowReader reader = readerFactory.openSheet(path, p.sheet())) {
                    List<String> missing = missingColumns(importer.requiredColumns(), reader.headers());
                    if (!missing.isEmpty()) {
                        fail(jobId, "Feuille « " + p.sheet() + " » — colonnes manquantes : "
                                + String.join(", ", missing), start);
                        return;
                    }
                    total += count(reader);
                }
            }
            importJobRepository.markRunning(jobId, total, OffsetDateTime.now());

            Counters c = new Counters();
            int chunk = Math.max(1, properties.chunkSize());
            long sinceUpdate = 0;
            for (SheetPlan p : present) {
                RowImporter importer = registry.forType(p.type());
                try (RowReader reader = readerFactory.openSheet(path, p.sheet())) {
                    for (RowData row : reader) {
                        if (sinceUpdate == 0 && Boolean.TRUE.equals(importJobRepository.isCancelRequested(jobId))) {
                            importJobRepository.finish(jobId, ImportStatus.CANCELLED, c.processed, c.imported,
                                    c.updated, c.skipped, c.failed, percent(c.processed, total),
                                    OffsetDateTime.now(), System.currentTimeMillis() - start);
                            log.info("Import système {} annulé après {} lignes", jobId, c.processed);
                            return;
                        }
                        apply(jobId, importer, row, job, c, p.sheet());
                        c.processed++;
                        if (++sinceUpdate >= chunk) {
                            importJobRepository.saveProgress(jobId, c.processed, c.imported, c.updated,
                                    c.skipped, c.failed, percent(c.processed, total));
                            sinceUpdate = 0;
                        }
                    }
                }
            }

            importJobRepository.finish(jobId, ImportStatus.COMPLETED, c.processed, c.imported, c.updated,
                    c.skipped, c.failed, 100, OffsetDateTime.now(), System.currentTimeMillis() - start);
            log.info("Import système {} terminé : {} importées, {} ignorées, {} en échec",
                    jobId, c.imported, c.skipped, c.failed);
        } catch (Exception ex) {
            log.error("Import système {} en échec", jobId, ex);
            fail(jobId, truncate(ex.getMessage()), start);
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

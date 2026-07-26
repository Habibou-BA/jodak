package com.jodak.admin.services;

import com.jodak.admin.dataimport.ImportExecutor;
import com.jodak.admin.dataimport.ImportFileStorage;
import com.jodak.admin.dataimport.RowImporterRegistry;
import com.jodak.admin.dtos.ImportErrorResponse;
import com.jodak.admin.dtos.ImportJobResponse;
import com.jodak.admin.entities.ImportJob;
import com.jodak.admin.entities.ImportJobRecord;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportFormat;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.admin.enums.ImportStatus;
import com.jodak.admin.exceptions.ImportValidationException;
import com.jodak.admin.mappers.ImportJobMapper;
import com.jodak.admin.repositories.ImportJobErrorRepository;
import com.jodak.admin.repositories.ImportJobRecordRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import com.jodak.dtos.common.PageResponse;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Orchestration des jobs d'import : création (stockage + déclenchement asynchrone), consultation,
 * annulation, rapport d'erreurs et compensation (rollback).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository importJobRepository;
    private final ImportJobErrorRepository importJobErrorRepository;
    private final ImportJobRecordRepository importJobRecordRepository;
    private final ImportFileStorage importFileStorage;
    private final RowImporterRegistry importerRegistry;
    private final ImportExecutor importExecutor;
    private final ImportJobMapper mapper;
    private final AthleteRepository athleteRepository;
    private final DisciplineRepository disciplineRepository;

    public ImportJobResponse create(MultipartFile file, ImportJobType jobType, ImportFormat format,
                                    ImportMode mode, DuplicateStrategy strategy, Long adminId) {
        if (!importerRegistry.supports(jobType)) {
            throw new ImportValidationException("Type d'import non pris en charge : " + jobType);
        }
        if (format != ImportFormat.CSV) {
            throw new ImportValidationException("Seul le format CSV est pris en charge pour le moment.");
        }
        ImportFileStorage.StoredFile stored = importFileStorage.store(file);
        if (importJobRepository.existsByContentHashAndJobTypeAndStatusIn(stored.hash(), jobType,
                List.of(ImportStatus.PENDING, ImportStatus.RUNNING))) {
            throw new ConflictException("Un import identique est déjà en attente ou en cours.");
        }
        ImportJob job = importJobRepository.save(ImportJob.builder()
                .jobType(jobType).format(format).mode(mode).duplicateStrategy(strategy)
                .status(ImportStatus.PENDING)
                .sourceFileName(file.getOriginalFilename() == null ? "import" : file.getOriginalFilename())
                .storedFilePath(stored.path().toString())
                .fileSize(stored.size()).contentHash(stored.hash())
                .correlationId(UUID.randomUUID().toString())
                .createdBy(adminId)
                .build());

        importExecutor.run(job.getId()); // exécution asynchrone
        log.info("Import job {} créé (type={}, mode={})", job.getId(), jobType, mode);
        return mapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public ImportJobResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ImportJobResponse> search(ImportStatus status, Pageable pageable) {
        Page<ImportJob> page = (status == null)
                ? importJobRepository.findAll(pageable)
                : importJobRepository.findByStatus(status, pageable);
        return PageResponseFactory.from(page, mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<ImportErrorResponse> errors(Long id, Pageable pageable) {
        findOrThrow(id);
        return PageResponseFactory.from(
                importJobErrorRepository.findByImportJobIdOrderByRowNumberAsc(id, pageable),
                mapper::toErrorResponse);
    }

    public void cancel(Long id) {
        ImportJob job = findOrThrow(id);
        if (job.isTerminal()) {
            throw new ConflictException("Le job est déjà terminé ; annulation impossible.");
        }
        importJobRepository.requestCancel(id);
        log.info("Annulation demandée pour l'import job {}", id);
    }

    /** Compense un import committé en supprimant les entités créées (ordre inverse d'insertion). */
    @Transactional
    public ImportJobResponse rollback(Long id) {
        ImportJob job = findOrThrow(id);
        if (job.getMode() != ImportMode.COMMIT) {
            throw new ConflictException("Rien à annuler pour un import en mode simulation.");
        }
        if (job.getStatus() == ImportStatus.PENDING || job.getStatus() == ImportStatus.RUNNING) {
            throw new ConflictException("Impossible d'annuler un import encore en cours.");
        }
        List<ImportJobRecord> records = importJobRecordRepository.findByImportJobIdOrderByIdDesc(id);
        for (ImportJobRecord record : records) {
            if ("INSERT".equals(record.getOperation())) {
                switch (record.getEntityType()) {
                    case "ATHLETE" -> athleteRepository.deleteById(record.getEntityId());
                    case "DISCIPLINE" -> disciplineRepository.deleteById(record.getEntityId());
                    default -> log.warn("Type non compensable : {}", record.getEntityType());
                }
            }
        }
        importJobRecordRepository.deleteAll(records);
        log.info("Import job {} compensé : {} enregistrement(s) supprimé(s)", id, records.size());
        return mapper.toResponse(findOrThrow(id));
    }

    private ImportJob findOrThrow(Long id) {
        return importJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Import introuvable pour l'identifiant " + id + "."));
    }
}

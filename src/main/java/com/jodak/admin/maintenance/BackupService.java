package com.jodak.admin.maintenance;

import com.jodak.admin.dtos.BackupResponse;
import com.jodak.admin.maintenance.DataExportService.ArchiveResult;
import com.jodak.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Sauvegarde logique : écrit l'archive d'export sur disque, vérifie l'écriture et son empreinte.
 * Si la génération ou l'écriture échoue, aucune donnée n'est modifiée.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DataExportService dataExportService;
    private final BackupProperties properties;

    public BackupResponse create(String adminEmail) {
        ArchiveResult archive = dataExportService.build(adminEmail);
        try {
            Path directory = Path.of(properties.storageDir());
            Files.createDirectories(directory);
            String fileName = "backup-" + LocalDateTime.now().format(TS) + "-"
                    + UUID.randomUUID().toString().substring(0, 8) + ".zip";
            Path file = directory.resolve(fileName);
            Files.write(file, archive.content());
            if (!Files.exists(file) || Files.size(file) != archive.content().length) {
                throw new IllegalStateException("La sauvegarde n'a pas été écrite correctement.");
            }
            log.info("Sauvegarde créée : {} ({} octets)", fileName, archive.content().length);
            return new BackupResponse(fileName, archive.content().length, archive.checksum(),
                    archive.manifest().records(), OffsetDateTime.now());
        } catch (IOException ex) {
            throw new IllegalStateException("Échec de l'écriture de la sauvegarde.", ex);
        }
    }

    public Path resolveExisting(String fileName) {
        String safe = Path.of(fileName).getFileName().toString();
        Path file = Path.of(properties.storageDir()).resolve(safe);
        if (!Files.exists(file)) {
            throw new ResourceNotFoundException("Sauvegarde introuvable : " + safe);
        }
        return file;
    }
}

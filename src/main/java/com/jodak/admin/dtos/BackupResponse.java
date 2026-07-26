package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Résultat d'une sauvegarde logique.
 */
@Schema(description = "Sauvegarde créée")
public record BackupResponse(
        String fileName,
        long sizeBytes,
        String checksum,
        Map<String, Long> records,
        OffsetDateTime createdAt
) {
}

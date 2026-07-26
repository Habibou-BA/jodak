package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Résultat d'une réinitialisation : sauvegarde préalable + nombre d'enregistrements supprimés.
 */
@Schema(description = "Résultat de la réinitialisation")
public record ResetResponse(
        String backupFileName,
        String backupChecksum,
        Map<String, Long> deleted
) {
}

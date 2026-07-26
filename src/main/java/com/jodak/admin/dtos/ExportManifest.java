package com.jodak.admin.dtos;

import java.util.Map;

/**
 * Métadonnées d'une archive d'export (sérialisées dans {@code metadata.json}).
 */
public record ExportManifest(
        String generatedAt,
        String applicationVersion,
        String admin,
        Map<String, Long> records,
        Map<String, String> checksums
) {
}

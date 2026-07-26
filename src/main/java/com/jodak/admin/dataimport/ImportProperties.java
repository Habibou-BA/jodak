package com.jodak.admin.dataimport;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de l'import (préfixe {@code import}).
 */
@ConfigurationProperties(prefix = "import")
public record ImportProperties(
        String storageDir,
        int chunkSize,
        int maxConcurrentJobs,
        long maxFileSizeBytes,
        long maxUncompressedBytes,
        double minInflateRatio
) {
}

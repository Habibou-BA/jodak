package com.jodak.admin.dtos;

import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportFormat;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.admin.enums.ImportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * État d'un job d'import.
 */
@Schema(description = "Job d'import")
public record ImportJobResponse(
        Long id,
        ImportJobType jobType,
        ImportFormat format,
        ImportMode mode,
        DuplicateStrategy duplicateStrategy,
        ImportStatus status,
        String sourceFileName,
        long totalRows,
        long processedRows,
        long importedRows,
        long updatedRows,
        long skippedRows,
        long failedRows,
        int progressPercent,
        boolean cancelRequested,
        String errorMessage,
        OffsetDateTime createdAt,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Long durationMs
) {
}

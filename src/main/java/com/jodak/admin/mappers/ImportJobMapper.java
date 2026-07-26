package com.jodak.admin.mappers;

import com.jodak.admin.dtos.ImportErrorResponse;
import com.jodak.admin.dtos.ImportJobResponse;
import com.jodak.admin.entities.ImportJob;
import com.jodak.admin.entities.ImportJobError;
import org.springframework.stereotype.Component;

/**
 * Conversion manuelle des entités d'import vers leurs DTO.
 */
@Component
public class ImportJobMapper {

    public ImportJobResponse toResponse(ImportJob job) {
        return new ImportJobResponse(
                job.getId(), job.getJobType(), job.getFormat(), job.getMode(),
                job.getDuplicateStrategy(), job.getStatus(), job.getSourceFileName(),
                job.getTotalRows(), job.getProcessedRows(), job.getImportedRows(),
                job.getUpdatedRows(), job.getSkippedRows(), job.getFailedRows(),
                job.getProgressPercent(), job.isCancelRequested(), job.getErrorMessage(),
                job.getCreatedAt(), job.getStartedAt(), job.getFinishedAt(), job.getDurationMs());
    }

    public ImportErrorResponse toErrorResponse(ImportJobError error) {
        return new ImportErrorResponse(
                error.getRowNumber(), error.getColumnName(), error.getRawValue(),
                error.getErrorCode(), error.getMessage());
    }
}
